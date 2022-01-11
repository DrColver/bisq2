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

package bisq.desktop.primary.main.content.settings.networkinfo;

import bisq.application.DefaultServiceProvider;
import bisq.desktop.NavigationTarget;
import bisq.desktop.common.view.Controller;
import bisq.desktop.common.view.TabController;
import bisq.desktop.primary.main.content.settings.networkinfo.transport.TransportTypeController;
import bisq.network.p2p.node.transport.Transport;
import lombok.Getter;

import java.util.Optional;

public class NetworkInfoController extends TabController {
    private final DefaultServiceProvider serviceProvider;
    @Getter
    private final NetworkInfoModel model;
    @Getter
    private final NetworkInfoView view;

    public NetworkInfoController(DefaultServiceProvider serviceProvider) {
        super(NavigationTarget.NETWORK_INFO);

        this.serviceProvider = serviceProvider;
        model = new NetworkInfoModel(serviceProvider);
        view = new NetworkInfoView(model, this);
    }

    @Override
    public void onNavigate(NavigationTarget navigationTarget, Optional<Object> data) {
        super.onNavigate(navigationTarget, data);
    }

    @Override
    protected Optional<Controller> createController(NavigationTarget navigationTarget, Optional<Object> data) {
        switch (navigationTarget) {
            case CLEAR_NET -> {
                return Optional.of(new TransportTypeController(serviceProvider, Transport.Type.CLEAR));
            }
            case TOR -> {
                return Optional.of(new TransportTypeController(serviceProvider, Transport.Type.TOR));
            }
            case I2P -> {
                return Optional.of(new TransportTypeController(serviceProvider, Transport.Type.I2P));
            }
            default -> {
                return Optional.empty();
            }
        }
    }
/*
    @Override
    public void onViewAttached() {
        super.onViewAttached();
        model.getSupportedTransportTypes().stream()
                .min(Enum::compareTo)
                .map(model::getNavigationTargetFromTransportType)
                .ifPresent(Navigation::navigateTo);
    }*/
}