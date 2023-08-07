package com.jupiterr.gem.mixin;

import com.jupiterr.gem.GEMMod;
import com.jupiterr.gem.waypoints.ChatWaypointTrigger;
import com.jupiterr.gem.waypoints.WaypointTrigger;
import com.jupiterr.gem.waypoints.WaypointTriggerTypes;
import com.jupiterr.gem.waypoints.Waypoint;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CancellationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ChatHud.class)
public class ChatHudMixin {
    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"))
    public void addMessage(Text text, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player == null) return;

        String msg = text.getString().replaceAll("§\\w", "");
        System.out.println(msg);

        if (!GEMMod.skipCheck) {

            Pattern completionPattern = Pattern.compile("\\w+ has ((.+)) first time!");
            Matcher completionMatcher = completionPattern.matcher(msg);

            if (msg.matches("Welcome \\w+ to the server!")) {
                Random r = new Random();

                sendMsg(GEMMod.welcomeMessages[r.nextInt(GEMMod.welcomeMessages.length - 1)]);
            }
        }

        boolean foundNext = false;
        List<Waypoint> toRemove = new ArrayList<>();
        List<Waypoint> toAdd = new ArrayList<>();

        for (Waypoint renderedPoint : GEMMod.renderedQuestWaypoints) {
            for (WaypointTrigger nextWaypointTrigger : renderedPoint.nextWaypointTriggers()) {
                if (nextWaypointTrigger.type() == WaypointTriggerTypes.CHAT) {
                    ChatWaypointTrigger chatTrigger = (ChatWaypointTrigger) nextWaypointTrigger;
                    if (msg.matches(chatTrigger.regex())) {
                        toRemove.add(renderedPoint);

                        for (Waypoint nextWaypoint : GEMMod.questWaypoints) {
                            if (nextWaypoint.identifier().equals(chatTrigger.next())) {
                                if (!renderedPoint.name().equals("start"))
                                    foundNext = true;
                                if (!GEMMod.renderedQuestWaypoints.contains(nextWaypoint)) {
                                    toAdd.add(nextWaypoint);
                                }
                            }
                        }
                    }
                }
            }
        }

        GEMMod.renderedQuestWaypoints.removeAll(toRemove);
        GEMMod.renderedQuestWaypoints.addAll(toAdd);

        if (!foundNext) {
            System.out.println("looking through current ones");
            for (Waypoint waypoint : GEMMod.questWaypoints) {
                if (waypoint.name() != null) {
                    if (waypoint.name().equals("start")) {
                        boolean isStarted = false;
                        //starting point
                        //check if the quest line is already ongoing
                        // #TODO MAKE PREVIOUS QUEST POINTS NOT APPEAR IF THERE IS ONE AHEAD
                        String regex = waypoint.identifier() + "\\d+";
                        for (Waypoint renderedWaypoint : GEMMod.renderedQuestWaypoints) {
                            if (renderedWaypoint.identifier().matches(regex)) {
                                System.out.println("already started quest");
                                isStarted = true;
                            }
                        }

                        if (isStarted) continue;
                    }
                }

                for (WaypointTrigger waypointTrigger : waypoint.nextWaypointTriggers()) {
                    if (waypointTrigger.type().equals(WaypointTriggerTypes.CHAT)) {
                        ChatWaypointTrigger chatTrigger = (ChatWaypointTrigger) waypointTrigger;
                        System.out.println(chatTrigger.regex());
                        if (msg.matches(chatTrigger.regex())) {
                            GEMMod.renderedQuestWaypoints.remove(waypoint);

                            if (!waypointTrigger.next().equals("end")) {
                                for (Waypoint nextWaypoint : GEMMod.questWaypoints) {
                                    if (nextWaypoint.identifier().equals(waypointTrigger.next())) {
                                        System.out.println("identifier found");
                                        if (!GEMMod.renderedQuestWaypoints.contains(nextWaypoint)) {
                                            GEMMod.renderedQuestWaypoints.add(nextWaypoint);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void sendMsg(String msg) {
        try {
            GEMMod.skipCheck = true;
//            MinecraftClient.getInstance().player.sendChatMessage("/g " + msg);

            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(200);
                    GEMMod.skipCheck = false;
                } catch (InterruptedException ignore) {
                }
            });

            thread.start();
        } catch (CancellationException ignored) {
        }
    }

    private void sentGG() {
        try {
            GEMMod.skipGG = true;

            Thread thread = new Thread(() -> {
                try{
                    Thread.sleep(10000);
                    GEMMod.skipGG = false;
                } catch (InterruptedException ignore) {}
            });

            thread.start();
        } catch (CancellationException ignore) {}
    }

    private void sendClientMsg(String msg) {
        try {
            GEMMod.skipCheck = true;
            assert MinecraftClient.getInstance().player != null;
            MinecraftClient.getInstance().player.sendMessage(Text.of(msg), false);

            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(200);
                    GEMMod.skipCheck = false;
                } catch (InterruptedException ignore) {
                }
            });

            thread.start();
        } catch (CancellationException ignored) {
        }
    }
}
