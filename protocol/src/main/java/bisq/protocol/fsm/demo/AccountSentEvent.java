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

package bisq.protocol.fsm.demo;

import bisq.protocol.fsm.Event;
import bisq.protocol.fsm.EventHandler;
import bisq.protocol.fsm.EventType;
import lombok.Getter;

@Getter
public class AccountSentEvent implements Event {
    public final static BisqEasyEventType TYPE = BisqEasyEventType.ON_ACCOUNT_SENT;

    public AccountSentEvent() {
    }

    public EventType getEventType() {
        return TYPE;
    }

    @Override
    public EventHandler getHandler() {
        return new AccountSentEventHandler();
    }
}