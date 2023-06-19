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

package bisq.desktop.primary.main.content.trade_apps.bisqEasy.chat.trade_state.states;

import bisq.application.DefaultApplicationService;
import bisq.chat.bisqeasy.channel.priv.BisqEasyPrivateTradeChatChannel;
import bisq.desktop.components.containers.Spacer;
import bisq.desktop.components.controls.BisqText;
import bisq.desktop.components.controls.MaterialTextArea;
import bisq.desktop.components.overlay.Popup;
import bisq.i18n.Res;
import bisq.trade.TradeException;
import bisq.trade.bisq_easy.BisqEasyTrade;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SellerState1 extends BaseState {
    private final Controller controller;

    public SellerState1(DefaultApplicationService applicationService, BisqEasyTrade bisqEasyTrade, BisqEasyPrivateTradeChatChannel channel) {
        controller = new Controller(applicationService, bisqEasyTrade, channel);
    }

    public View getView() {
        return controller.getView();
    }

    private static class Controller extends BaseState.Controller<Model, View> {
        private Controller(DefaultApplicationService applicationService, BisqEasyTrade bisqEasyTrade, BisqEasyPrivateTradeChatChannel channel) {
            super(applicationService, bisqEasyTrade, channel);
        }

        @Override
        protected Model createModel(BisqEasyTrade bisqEasyTrade, BisqEasyPrivateTradeChatChannel channel) {
            return new Model(bisqEasyTrade, channel);
        }

        @Override
        protected View createView() {
            return new View(model, this);
        }

        @Override
        public void onActivate() {
            super.onActivate();
            model.getButtonDisabled().bind(model.getPaymentAccountData().isEmpty());
            findUsersAccountData().ifPresent(accountData -> model.getPaymentAccountData().set(accountData));
        }

        @Override
        public void onDeactivate() {
            super.onDeactivate();
            model.getButtonDisabled().unbind();
        }

        private void onSendPaymentData() {
            String message = Res.get("bisqEasy.tradeState.info.seller.phase1.chatBotMessage", model.getPaymentAccountData().get());
            sendChatBotMessage(message);
            try {
                bisqEasyTradeService.sellerSendsPaymentAccount(model.getBisqEasyTrade(), model.getPaymentAccountData().get());
            } catch (TradeException e) {
                new Popup().error(e).show();
            }
        }
    }

    @Getter
    private static class Model extends BaseState.Model {
        private final StringProperty paymentAccountData = new SimpleStringProperty();
        private final BooleanProperty buttonDisabled = new SimpleBooleanProperty();

        protected Model(BisqEasyTrade bisqEasyTrade, BisqEasyPrivateTradeChatChannel channel) {
            super(bisqEasyTrade, channel);
        }
    }

    public static class View extends BaseState.View<Model, Controller> {
        private final Button button;
        private final MaterialTextArea paymentAccountData;

        private View(Model model, Controller controller) {
            super(model, controller);

            BisqText infoHeadline = new BisqText(Res.get("bisqEasy.tradeState.info.seller.phase1.headline"));
            infoHeadline.getStyleClass().add("bisq-easy-trade-state-info-headline");

            paymentAccountData = FormUtils.addTextArea(Res.get("bisqEasy.tradeState.info.seller.phase1.accountData"), "", true);
            paymentAccountData.setPromptText(Res.get("bisqEasy.tradeState.info.seller.phase1.accountData.prompt"));

            button = new Button(Res.get("bisqEasy.tradeState.info.seller.phase1.buttonText"));
            button.setDefaultButton(true);

            Label helpLabel = FormUtils.getHelpLabel(Res.get("bisqEasy.tradeState.info.seller.phase1.note"));

            VBox.setMargin(button, new Insets(5, 0, 5, 0));
            root.getChildren().addAll(
                    infoHeadline,
                    paymentAccountData,
                    button,
                    Spacer.fillVBox(),
                    helpLabel);
        }

        @Override
        protected void onViewAttached() {
            super.onViewAttached();

            paymentAccountData.textProperty().bindBidirectional(model.getPaymentAccountData());
            button.disableProperty().bind(model.getButtonDisabled());
            button.setOnAction(e -> controller.onSendPaymentData());
        }

        @Override
        protected void onViewDetached() {
            super.onViewDetached();

            paymentAccountData.textProperty().unbindBidirectional(model.getPaymentAccountData());
            button.disableProperty().unbind();
            button.setOnAction(null);
        }
    }
}