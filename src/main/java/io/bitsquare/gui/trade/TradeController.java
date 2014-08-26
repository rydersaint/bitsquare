/*
 * This file is part of Bitsquare.
 *
 * Bitsquare is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bitsquare is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bitsquare. If not, see <http://www.gnu.org/licenses/>.
 */

package io.bitsquare.gui.trade;

import io.bitsquare.di.GuiceFXMLLoader;
import io.bitsquare.gui.CachedViewController;
import io.bitsquare.gui.NavigationItem;
import io.bitsquare.gui.ViewController;
import io.bitsquare.gui.components.ValidatingTextField;
import io.bitsquare.gui.trade.createoffer.CreateOfferCodeBehind;
import io.bitsquare.gui.trade.orderbook.OrderBookController;
import io.bitsquare.gui.trade.takeoffer.TakerOfferController;
import io.bitsquare.trade.Direction;

import java.io.IOException;

import java.net.URL;

import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.control.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkArgument;

public class TradeController extends CachedViewController {
    private static final Logger log = LoggerFactory.getLogger(TradeController.class);

    protected OrderBookController orderBookController;
    protected CreateOfferCodeBehind createOfferCodeBehind;
    protected TakerOfferController takerOfferController;
    protected GuiceFXMLLoader orderBookLoader;


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        super.initialize(url, rb);

        loadViewAndGetChildController(NavigationItem.ORDER_BOOK);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void activate() {
        super.activate();

        applyDirection();

        // TODO find better solution
        // Textfield focus out triggers validation, use runLater as quick fix...
        ((TabPane) root).getSelectionModel().selectedIndexProperty().addListener((observableValue) ->
                                                                                         Platform.runLater(() -> ValidatingTextField.hidePopover()));
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Navigation
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public ViewController loadViewAndGetChildController(NavigationItem navigationItem) {
        TabPane tabPane = (TabPane) root;
        if (navigationItem == NavigationItem.ORDER_BOOK) {
            checkArgument(orderBookLoader == null);
            // Orderbook must not be cached by GuiceFXMLLoader as we use 2 instances for sell and buy screens.
            orderBookLoader =
                    new GuiceFXMLLoader(getClass().getResource(NavigationItem.ORDER_BOOK.getFxmlUrl()), false);
            try {
                final Parent view = orderBookLoader.load();
                final Tab tab = new Tab("Orderbook");
                tab.setClosable(false);
                tab.setContent(view);
                tabPane.getTabs().add(tab);
            } catch (IOException e) {
                log.error(e.getMessage());
            }
            orderBookController = orderBookLoader.getController();
            orderBookController.setParentController(this);
            return orderBookController;
        }
        else if (navigationItem == NavigationItem.CREATE_OFFER) {
            checkArgument(createOfferCodeBehind == null);

            // CreateOffer and TakeOffer must not be cached by GuiceFXMLLoader as we cannot use a view multiple times
            // in different graphs
            GuiceFXMLLoader loader = new GuiceFXMLLoader(getClass().getResource(navigationItem.getFxmlUrl()), false);
            try {
                final Parent view = loader.load();
                createOfferCodeBehind = loader.getController();
                createOfferCodeBehind.setParentController(this);
                final Tab tab = new Tab("Create offer");
                tab.setContent(view);
                tabPane.getTabs().add(tab);
                tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1);
                return createOfferCodeBehind;
            } catch (IOException e) {
                log.error(e.getMessage());
            }
            return null;
        }
        else if (navigationItem == NavigationItem.TAKE_OFFER) {
            checkArgument(takerOfferController == null);

            // CreateOffer and TakeOffer must not be cached by GuiceFXMLLoader as we cannot use a view multiple times
            // in different graphs
            GuiceFXMLLoader loader = new GuiceFXMLLoader(getClass().getResource(navigationItem.getFxmlUrl()), false);
            try {
                final Parent view = loader.load();
                takerOfferController = loader.getController();
                takerOfferController.setParentController(this);
                final Tab tab = new Tab("Take offer");
                tab.setContent(view);
                tabPane.getTabs().add(tab);
                tabPane.getSelectionModel().select(tabPane.getTabs().size() - 1);
                return takerOfferController;
            } catch (IOException e) {
                log.error(e.getMessage());
            }
            return null;
        }
        else {
            log.error("navigationItem not supported: " + navigationItem);
            return null;
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Public
    ///////////////////////////////////////////////////////////////////////////////////////////

    public void onCreateOfferViewRemoved() {
        createOfferCodeBehind = null;

        orderBookController.onCreateOfferViewRemoved();
    }

    public void onTakeOfferViewRemoved() {
        takerOfferController = null;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Protected
    ///////////////////////////////////////////////////////////////////////////////////////////

    // Template method to be overwritten by sub class.
    protected void applyDirection() {
        orderBookController.applyDirection(Direction.SELL);
    }


}
