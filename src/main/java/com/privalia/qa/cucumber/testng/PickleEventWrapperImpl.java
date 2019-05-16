package com.privalia.qa.cucumber.testng;

import cucumber.api.testng.PickleEventWrapper;
import gherkin.events.PickleEvent;

class PickleEventWrapperImpl implements PickleEventWrapper {

    private final PickleEvent pickleEvent;

    PickleEventWrapperImpl(PickleEvent pickleEvent) {
        this.pickleEvent = pickleEvent;
    }

    public PickleEvent getPickleEvent() {
        return pickleEvent;
    }

    @Override
    public String toString() {
        return "\"" + pickleEvent.pickle.getName() + "\"";
    }
}