package com.cryonix.launcher.minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LaunchPlan {
    private final List<String> arguments;

    public LaunchPlan(List<String> arguments) {
        this.arguments = Collections.unmodifiableList(new ArrayList<>(arguments));
    }

    public List<String> arguments() {
        return arguments;
    }
}