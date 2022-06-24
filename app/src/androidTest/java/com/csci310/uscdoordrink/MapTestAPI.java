package com.csci310.uscdoordrink;

import android.app.Activity;
import android.graphics.PointF;
import android.util.Log;
import android.view.View;

import androidx.test.uiautomator.UiDevice;

import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;

public class MapTestAPI {
    private final long INITIALIZE_TIME_MS = 2000;
    private SymbolManager symbols = null;

    public MapTestAPI() throws Exception {
        try {
            Thread.sleep(INITIALIZE_TIME_MS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (MapPage.getSymbols() == null) {
            throw new Exception("Map was not initialized/run.");
        }
        symbols = MapPage.getSymbols();
    }

    public Symbol findSymbolFromMerchantName(String name) throws Exception {
        for (int i = 0; i < symbols.getAnnotations().size(); i++) {
            Symbol symbol = symbols.getAnnotations().get(i);
            if (symbol.getData().getAsJsonObject().get("name").getAsString().equals(name)) {
                return symbol;
            }
        }
        throw new Exception("Symbol was not found");
    }

    /**
     * Symbol - symbol to click on
     * Activity - used to runOnUIThread
     * UIDevice - used to preform click action
     * Boolean - used to denote whether activity was successfully clicked on.
     */
    public void clickOnSymbol(Symbol symbol, Activity activity, UiDevice uiDevice) throws InterruptedException {
        boolean[] clicked = {false};
        final int[] x = new int[1];
        final int[] y = new int[1];
        final boolean[] done = new boolean[1];
        try {
            Symbol finalTestSymbol = symbol;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PointF f = MapPage.symbolCoords(finalTestSymbol);

                    View v = MapPage.mapView;
                    int[] loc = new int[2];
                    v.getLocationOnScreen(loc);

                    x[0] = (int)f.x;
                    y[0] = (int)f.y + loc[1];
                    done[0] = true;
                }
            });
        } catch (Throwable throwable) {
            clicked[0] = false;
            throwable.printStackTrace();
        }

        while(!done[0]) {
            Thread.sleep(250);
            Log.e("", "waiting for UI Thread");
        }
        Log.e("", x[0] + " " + y[0]);
        uiDevice.click(x[0], y[0]);
        clicked[0] = true;
        try {
            Thread.sleep(2250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public SymbolManager getSymbols() {
        return symbols;
    }



}