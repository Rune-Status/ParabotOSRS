package org.osrs.min.api.interaction;


import org.osrs.min.api.data.Game;
import org.osrs.min.api.interfaces.Interactable;
import org.osrs.min.api.wrappers.*;
import org.osrs.min.canvas.inputs.Mouse;
import org.parabot.api.calculations.Random;
import org.parabot.core.ui.Logger;
import org.parabot.environment.api.utils.Time;

import java.awt.*;
import java.util.function.Supplier;

public class InteractionHandler {

    public static MenuAction nextInteraction = null;
    private Interactable interactable;
    private boolean debug = false;

    public InteractionHandler(Interactable interactable) {
        this.interactable = interactable;
    }

    public static MenuAction getNextInteraction() {
        return nextInteraction;
    }

    public static void setNextInteraction(MenuAction nextInteraction) {
        InteractionHandler.nextInteraction = nextInteraction;
    }

    public MenuAction getAction(int opcode) {
        if (interactable instanceof NPC) {
            return new MenuAction("", ((NPC) interactable).getName(), opcode, ((NPC) interactable).getIndex(), 0, 0);
        } else if (interactable instanceof Player) {
            return new MenuAction("", ((Player) interactable).getName(), opcode, ((Player) interactable).getIndex(), 0, 0);
        }

        return null;
    }

    public MenuAction getAction(String action) {
        int opCode;
        if (action.equalsIgnoreCase("null")) {
            return new MenuAction(action, "", 1006, 0, 0, 0); //fake action
        }
        if (interactable instanceof NPC) {
            opCode = getOpcode(action, 9);
            if (action.equalsIgnoreCase("cast") && Game.isSpellSelected()) {
                opCode = 8;
            } else if (action.equalsIgnoreCase("examine")) {
                opCode = 1003;
            } else if (action.equalsIgnoreCase("use") && Game.isItemSelected()) {
                opCode = 7;
            }
            if (opCode != -1) {
                return new MenuAction(action, ((NPC) interactable).getName(), opCode, ((NPC) interactable).getIndex(), 0, 0);
            }
        } else if (interactable instanceof Player) {
            opCode = getOpcode(action, 44);
            if (action.equalsIgnoreCase("cast") && Game.isSpellSelected()) {
                opCode = 15;
            } else if (action.equalsIgnoreCase("use") && Game.isItemSelected()) {
                opCode = 14;
            }
            if (opCode != -1) {
                return new MenuAction(action, ((Player) interactable).getName(), opCode, ((Player) interactable).getIndex(), 0, 0);
            }
        } else if (interactable instanceof SceneObject) {
            opCode = getOpcode(action, 3);
            if (action.equals("Examine")) {
                opCode = 1002;
            } else if (action.equalsIgnoreCase("use")) {
                opCode = 1;
            }
            if (opCode != -1) {
                return new MenuAction(action, ((SceneObject) interactable).getDefinition().getName(), opCode, ((SceneObject) interactable).getId(), ((SceneObject) interactable).getLocalX(), ((SceneObject) interactable).getLocalY());
            }
        } else if (interactable instanceof GroundItem) {
            String[] actions = interactable.getActions();
            opCode = getOpcode(action, 18);
            if (opCode == -1 && (actions == null || actions[2] == null || actions[2].equals("Take")) && action.equalsIgnoreCase("Take")) {
                opCode = 20;
                return new MenuAction(action, "", opCode, ((GroundItem) interactable).getId(), ((GroundItem) interactable).getSceneX(), ((GroundItem) interactable).getSceneY());
            }
            return new MenuAction(action, "", opCode, ((GroundItem) interactable).getId(), ((GroundItem) interactable).getSceneX(), ((GroundItem) interactable).getSceneY());
        } else if (interactable instanceof Item) {
            opCode = getOpcode(action, 33);
            if (action.equals("Cast")) {
                opCode = 32;
                return new MenuAction(action, ((Item) interactable).getName(), opCode, ((Item) interactable).getId(), ((Item) interactable).getIndex(), ((Item) interactable).getHash());
            }
            if (!action.equals("Use")) {
                if (!action.equals("Drop")) {
                    return new MenuAction(action, ((Item) interactable).getName(), opCode, ((Item) interactable).getId(), ((Item) interactable).getIndex(), ((Item) interactable).getHash());
                } else if (action.equals("Drop")) {
                    opCode = 37;
                    return new MenuAction(action, ((Item) interactable).getName(), opCode, ((Item) interactable).getId(), ((Item) interactable).getIndex(), ((Item) interactable).getHash());
                }
                if (opCode != -1) {
                    return new MenuAction(action, ((Item) interactable).getName(), opCode, ((Item) interactable).getId(), ((Item) interactable).getIndex(), ((Item) interactable).getHash());
                }
                opCode = 58;
                return new MenuAction(action, ((Item) interactable).getName(), opCode, ((Item) interactable).getId(), ((Item) interactable).getIndex(), ((Item) interactable).getHash());
            }
            if (Game.isItemSelected()) {
                opCode = 31;
                return new MenuAction(action, ((Item) interactable).getName(), opCode, ((Item) interactable).getId(), ((Item) interactable).getIndex(), ((Item) interactable).getHash());
            }
            opCode = 58;
            return new MenuAction(action, ((Item) interactable).getName(), opCode, ((Item) interactable).getId(), ((Item) interactable).getIndex(), ((Item) interactable).getHash());
        }

        return null;
    }

    private int getSpellTargets(int config) {
        return config >> 11 & 63;
    }


    private int getOpcode(String action, int opcodeOffset) {
        int i = indexOf(interactable.getActions(), action);
        if (opcodeOffset == 3 && i == 4) {
            return 1001;
        }
        if (i < 0) return -1;
        return i + opcodeOffset;
    }

    private int indexOf(String[] actions, String action) {
        int i;
        if (action == null) return -1;
        if (actions == null) {
            return -1;
        }
        int n = i = 0;
        while (n < actions.length) {
            String s = actions[i];
            if (action.equalsIgnoreCase(s)) {
                return i;
            }
            n = ++i;
        }
        return -1;
    }

    public boolean interact(String actionText) {
        return this.interact(() -> getAction(actionText));
    }


    public boolean interact(int opcode) {
        return this.interact(() -> getAction(opcode));
    }

    private boolean interact(Supplier<MenuAction> supplier) {
        if (Game.isMenuOpen()) {
            Logger.addMessage("MENU OPEN! Tell the lazy dev to add a handler to close the menu.");
            return false;
        }

        if (needsRandomSpam()) {
            sendRandomInteractions();
        }

        MenuAction menuAction = supplier.get();
        if (menuAction == null) return false;
        Point point = new Point(Random.between(10, 500), Random.between(7, 500));
        setNextInteraction(menuAction);
        Game.setForcingAction(true);
        Time.sleep(Random.between(50, 150));
        Mouse.getInstance().click(point, false);
        if (debug) {
            System.out.println("Opcode: " + menuAction.getOpcode() + " Primary: " + menuAction.getPrimaryArg() + " : " + menuAction.getSecondaryArg() + " : " + menuAction.getTertiaryArg());
        }
        return true;
    }

    private boolean sendRandomInteractions() {
        System.out.println("Sending random action spam."); //does this antiban even work?
        for (int i = 0; i < Random.between(1, 8); i++) {
            MenuAction menuAction = getAction("null");
            Point point = new Point(Random.between(10, 500), Random.between(7, 500));
            setNextInteraction(menuAction);
            Game.setForcingAction(true);
            Mouse.getInstance().click(point, false);
            Time.sleep(Random.between(3, 10));
        }
        return true;
    }

    private boolean needsRandomSpam() {
        return Random.between(1, 10) < 3;
    }
}
