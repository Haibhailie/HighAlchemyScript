package net.botwithus;

import net.botwithus.api.game.hud.inventories.Backpack;
import net.botwithus.api.game.hud.inventories.Bank;

import net.botwithus.internal.scripts.ScriptDefinition;
import net.botwithus.rs3.events.impl.ChatMessageEvent;
import net.botwithus.rs3.game.Area;
import net.botwithus.rs3.game.Client;
import net.botwithus.rs3.game.Coordinate;
import net.botwithus.rs3.game.Item;
import net.botwithus.rs3.game.hud.interfaces.Component;
import net.botwithus.rs3.game.hud.interfaces.Interfaces;
import net.botwithus.rs3.game.minimenu.MiniMenu;
import net.botwithus.rs3.game.minimenu.actions.ComponentAction;
import net.botwithus.rs3.game.minimenu.actions.SelectableAction;
import net.botwithus.rs3.game.queries.builders.components.ComponentQuery;
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery;
import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer;
import net.botwithus.rs3.game.scene.entities.object.SceneObject;
import net.botwithus.rs3.script.Execution;
import net.botwithus.rs3.script.LoopingScript;
import net.botwithus.rs3.script.config.ScriptConfig;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class HighAlchemyScript extends LoopingScript {

    private BotState botState = BotState.IDLE;
    private Random random = new Random();
    private volatile long profit = 0;
    private volatile long itemsAlched = 0;
    private volatile long runTime = 1;
    private volatile long startTime;

    public HighAlchemyScript(String s, ScriptConfig scriptConfig, ScriptDefinition scriptDefinition) {
        super(s, scriptConfig, scriptDefinition);
        this.sgc = new HighAlchemyGraphicsContext(getConsole(), this);
    }

    enum BotState {
        IDLE,
        ALCHING,
        BANKING,
    }

    public enum Spell {
        HIGH_ALCHEMY(47);

        private final int index;

        Spell(int index) {
            this.index = index;
        }

        private static final int CONTAINER_INDEX = 1461;
        private static final int COMPONENT_INDEX = 1;
        private static final int COMPONENT_UID = CONTAINER_INDEX << 16 | COMPONENT_INDEX;

        public boolean select() {
            return MiniMenu.interact(SelectableAction.SELECTABLE_COMPONENT.getType(), 0, index, COMPONENT_UID);
        }

        public boolean activate() {
            return MiniMenu.interact(ComponentAction.COMPONENT.getType(), 1, index, COMPONENT_UID);
        }

    }

    @Override
    public boolean initialize() {
        super.initialize();
        startTime = System.currentTimeMillis();
        subscribe(ChatMessageEvent.class, chatMessageEvent -> {
            if (chatMessageEvent.getMessage().contains("coins have been added to your money pouch.")) {
                try {
                    println("Received chat message: " + chatMessageEvent.getMessage());
                    String[] parts = chatMessageEvent.getMessage().split(" ");
                    parts[0] = parts[0].replaceAll("[^\\d]", "");
                    long addedProfit = Long.parseLong(parts[0]);
                    addProfit(addedProfit);
                    incrementItemsAlched();
                    println("Added profit: " + addedProfit + ", Total profit: " + profit);
                    println("Items alched: " + itemsAlched);
                } catch (NumberFormatException e) {
                    println("Failed to parse profit: " + e.getMessage());
                }
            }
        });
        return true;
    }

    private synchronized void addProfit(long amount) {
        profit += amount;
    }

    private synchronized void incrementItemsAlched() {
        itemsAlched++;
    }

    public long getRunTime() {
        return (System.currentTimeMillis() - startTime) / 1000; // Convert milliseconds to seconds
    }

    public long getProfit() {
        return profit;
    }

    public long getProfitPerHour() {
        if (getRunTime() > 0) {
            return (profit * 3600) / getRunTime(); // Convert per second profit to per hour
        }
        return 0;
    }

    public String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    public long getItemsAlched() {
        return itemsAlched;
    }

    @Override
    public void onLoop() {
        if (botState == BotState.IDLE) {
            println("Script is idle. No operations will be performed.");
            Execution.delay(random.nextLong(3000, 5000)); // Wait before checking the state again.
            return;
        }

        println("Script initialized.");
        Execution.delay(random.nextLong(1000, 3000));
        LocalPlayer player = Client.getLocalPlayer();
        if (player == null || Client.getGameState() != Client.GameState.LOGGED_IN) {
            println("Player is null or not logged in.");
            Execution.delay(random.nextLong(3000, 7000));
            return;
        }

        switch (botState) {
            case ALCHING -> {
                println("Proceeding with alchemy.");
                Execution.delay(handleAlchemy(player)); // handle your alchemy logic
            }
            case BANKING -> {
                println("Proceeding with banking.");
                Execution.delay(handleBanking(player)); // handle your banking logic
            }
            default -> {
                println("Unhandled state.");
                Execution.delay(random.nextLong(3000, 5000));
            }
        }
    }

    private long handleBanking(LocalPlayer player) {

        if (player.isMoving()) {
            return random.nextLong(3000, 5000);
        }

        if (Bank.isOpen()) {
            println("Bank is open!");
            botState = BotState.BANKING;
            Bank.loadLastPreset();
            return random.nextLong(1000, 3000);
        } else {
            SceneObject banker = SceneObjectQuery.newQuery().name("Banker").option("Bank").results().nearest();
            if (banker != null) {
                botState = BotState.IDLE;
                println("Bank not found.");
                return random.nextLong(1000, 3000);
            } else {
                botState = BotState.BANKING;
                println("Yay, we found our bank.");
                Bank.loadLastPreset();
            }
        }

        botState = BotState.ALCHING;
        return random.nextLong(4500, 6000);
    }

    private long handleAlchemy(LocalPlayer player) {
        if (player.isMoving()) {
            return random.nextLong(1500, 3000);
        }

        if (getBotState() == BotState.IDLE) {
            return random.nextLong(1500, 3000);
        }

        if (checkRequirements()) {
            List<Item> items = Backpack.getItems();
            if (items.size() > 1) {
                // Start iterating from the second item (index 1)
                for (int i = 1; i < items.size(); i++) {
                    if (getBotState() == BotState.IDLE) {
                        println("Bot state is idle. Stopping alchemy.");
                        return random.nextLong(1500, 3000);
                    }
                    Item item = items.get(i);
                    if (item != null) {
                        // Process each item as needed
                        println("Processing item: " + item.getName() + ", ID: " + item.getId() + ", Slot: " + i);
                        performHighAlchemy(item);
                    }
                }
            } else {
                println("Not enough items in backpack to skip the first one.");
            }
            botState = BotState.BANKING;
        } else {
            println("Requirements not met. Looks like you're out of nature runes or alchable items. Idling.");
            botState = BotState.IDLE;
        }
        return random.nextLong(1500, 3000);
    }

    private boolean checkRequirements() {
        List<Item> items = Backpack.getItems();
        boolean hasNatureRune = false;
        int itemCount = 0;

        for (Item item : items) {
            if (Objects.equals(item.getName(), "Nature rune")) {
                hasNatureRune = true;
                println("Nature rune found successfully.");
            } else {
                itemCount++;
                println("Alchable item found: " + item.getName() + ", ID: " + item.getId() + ", Slot: " + item.getSlot());
            }
        }
        return hasNatureRune && itemCount > 0;
    }

    private void performHighAlchemy(Item item) {
        Spell.HIGH_ALCHEMY.select();
        Execution.delay(random.nextLong(1000, 2000)); // A delay to ensure the spell is activated
        MiniMenu.interact(SelectableAction.SELECT_COMPONENT_ITEM.getType(), 0, item.getSlot(), 96534533);
        Execution.delay(random.nextLong(1500, 3000));
    }

    public BotState getBotState() {
        return botState;
    }

    public void setBotState(BotState botState) {
        this.botState = botState;
    }

}