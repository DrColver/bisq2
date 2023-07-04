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

package bisq.desktop.primary.splash;

import bisq.desktop.State;
import bisq.desktop.common.view.Model;
import bisq.network.p2p.node.Node;
import javafx.beans.property.*;
import lombok.Getter;

@Getter
public class SplashModel implements Model {
    private final ObjectProperty<State> applicationState = new SimpleObjectProperty<>();
    private final ObjectProperty<Node.State> clearServiceNodeState = new SimpleObjectProperty<>();
    private final ObjectProperty<Node.State> torServiceNodeState = new SimpleObjectProperty<>();
    private final ObjectProperty<Node.State> i2pServiceNodeState = new SimpleObjectProperty<>();
    private final StringProperty transportState = new SimpleStringProperty();
    private final DoubleProperty progress = new SimpleDoubleProperty(0);
}