/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.social.user.reputation;

import bisq.common.data.ByteArray;
import bisq.common.observable.Observable;
import bisq.network.NetworkService;
import bisq.network.p2p.services.data.DataService;
import bisq.network.p2p.services.data.storage.auth.AuthenticatedData;
import bisq.oracle.daobridge.model.AuthorizedProofOfBurnData;
import bisq.persistence.PersistenceService;
import bisq.security.DigestUtil;
import bisq.social.user.ChatUser;
import bisq.social.user.ChatUserService;
import com.google.common.base.Charsets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Slf4j
public class ReputationService implements DataService.Listener {
    private final PersistenceService persistenceService;
    private final NetworkService networkService;
    private final ChatUserService chatUserService;
    private final Map<ByteArray, Set<AuthorizedProofOfBurnData>> authorizedProofOfBurnDataSetByHash = new ConcurrentHashMap<>();
    private final Observable<Integer> reputationChanged = new Observable<>(0);
    private boolean isBatchProcessing;

    public ReputationService(PersistenceService persistenceService, NetworkService networkService, ChatUserService chatUserService) {
        this.persistenceService = persistenceService;
        this.networkService = networkService;
        this.chatUserService = chatUserService;
    }

    public CompletableFuture<Boolean> initialize() {
        log.info("initialize");
        networkService.addDataServiceListener(this);
        isBatchProcessing = true;
        networkService.getDataService()
                .ifPresent(dataService -> dataService.getAllAuthenticatedPayload()
                        .forEach(this::onAuthenticatedDataAdded));
        isBatchProcessing = false;
        if (!authorizedProofOfBurnDataSetByHash.isEmpty()) {
            authorizedProofOfBurnDataSetByHash.keySet().forEach(pubKeyHash -> {
                findAuthorizedProofOfBurnDataSet(pubKeyHash)
                        .ifPresent(set -> ReputationScoreCalculation.addTotalScore(pubKeyHash, set));
            });
            reputationChanged.set(reputationChanged.get() + 1);
        }
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public void onAuthenticatedDataAdded(AuthenticatedData authenticatedData) {
        if (authenticatedData.getDistributedData() instanceof AuthorizedProofOfBurnData authorizedProofOfBurnData) {
            addAuthorizedProofOfBurnData(authorizedProofOfBurnData);

            if (!isBatchProcessing) {
                ByteArray hash = new ByteArray(authorizedProofOfBurnData.getHash());
                findAuthorizedProofOfBurnDataSet(hash)
                        .ifPresent(set -> {
                            ReputationScoreCalculation.addTotalScore(hash, set);
                            reputationChanged.set(reputationChanged.get() + 1);
                        });
            }
        }
    }

    private Optional<Set<AuthorizedProofOfBurnData>> findAuthorizedProofOfBurnDataSet(ByteArray hash) {
        return Optional.ofNullable(authorizedProofOfBurnDataSetByHash.get(hash));
    }

    public Optional<ReputationScore> findReputationScore(ChatUser chatUser) {
        // We use the UTF8 bytes from the string preImage at the Bisq 1 proof of work tool
        byte[] preImage =  chatUser.getId().getBytes(Charsets.UTF_8);
        byte[] hashOfPreImage = DigestUtil.hash(preImage);
        ByteArray hash = new ByteArray(hashOfPreImage);
        return findAuthorizedProofOfBurnDataSet(hash)
                .map(set -> ReputationScoreCalculation.getReputationScore(hash));
    }

    private void addAuthorizedProofOfBurnData(AuthorizedProofOfBurnData authorizedProofOfBurnData) {
        ByteArray key = new ByteArray(authorizedProofOfBurnData.getHash());
        if (!authorizedProofOfBurnDataSetByHash.containsKey(key)) {
            authorizedProofOfBurnDataSetByHash.put(key, new HashSet<>());
        }
        authorizedProofOfBurnDataSetByHash.get(key).add(authorizedProofOfBurnData);
    }
}