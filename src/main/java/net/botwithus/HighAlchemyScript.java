package net.botwithus;

import net.botwithus.api.game.hud.inventories.Bank;

import net.botwithus.internal.scripts.ScriptDefinition;
import net.botwithus.rs3.events.impl.ChatMessageEvent;
import net.botwithus.rs3.game.Area;
import net.botwithus.rs3.game.Client;
import net.botwithus.rs3.game.Coordinate;
import net.botwithus.rs3.game.hud.interfaces.Component;
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery;
import net.botwithus.rs3.game.queries.results.ResultSet;
import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer;
import net.botwithus.rs3.game.scene.entities.object.SceneObject;
import net.botwithus.rs3.script.Execution;
import net.botwithus.rs3.script.LoopingScript;
import net.botwithus.rs3.script.config.ScriptConfig;

import java.util.Random;

public class HighAlchemyScript extends LoopingScript {

    private BotState botState = BotState.IDLE;
    private boolean someBool = true;
    private Random random = new Random();
    private final Area GE = new Area.Rectangular(new Coordinate(3156,3494,0), new Coordinate(3170,3486,0));

    public HighAlchemyScript(String s, ScriptConfig scriptConfig, ScriptDefinition scriptDefinition) {
        super(s, scriptConfig, scriptDefinition);
        this.sgc = new HighAlchemyGraphicsContext(getConsole(), this);
    }

    enum BotState {
        IDLE,
        ALCHING,
        BANKING,
    }

    @Override
    public boolean initialize() {
        super.initialize();
        subscribe(ChatMessageEvent.class, chatMessageEvent -> {
            //more events available at https://botwithus.net/javadoc/net.botwithus.rs3/net/botwithus/rs3/events/impl/package-summary.html
            println("Chatbox message received: %s", chatMessageEvent.getMessage());
        });
        return true;
    }

    @Override
    public void onLoop() {
        println("The loop has begun!");
        Execution.delay(random.nextLong(1000,3000));
        LocalPlayer player = Client.getLocalPlayer();
        assert player != null;
        handleBanking(player);

        if (player == null || Client.getGameState() != Client.GameState.LOGGED_IN || botState == BotState.IDLE) {
            println("Player is null or not logged in, or we're idle.");
            Execution.delay(random.nextLong(3000,7000));
            return;
        }

        switch (botState) {
            case IDLE -> {
                //do nothing
                println("We're idle!");
                Execution.delay(random.nextLong(1000,3000));
            }
            case ALCHING -> {
                //do some code that handles your alchemy
                Execution.delay(handleAlchemy(player));
            }
            case BANKING -> {
                //handle your banking logic, etc
                Execution.delay(handleBanking(player));
            }
        }
    }

    private long handleBanking(LocalPlayer player) {

        if (player.isMoving()) {
            return random.nextLong(3000,5000);
        }

        if (Bank.isOpen()) {
            println("Bank is open!");
            botState = BotState.BANKING;
            Bank.loadLastPreset();
            return random.nextLong(1000,3000);
        }
        else
        {
            SceneObject banker = SceneObjectQuery.newQuery().name("Banker").option("Bank").results().nearest();
            if (banker != null)
            {
                println("Bank not found.");
                botState = BotState.IDLE;
            }
            else
            {
                botState = BotState.BANKING;
                println("Yay, we found our bank.");
                Bank.loadLastPreset();
            }
        }

        botState = BotState.ALCHING;
        return random.nextLong(1500,3000);
    }

    private long handleAlchemy(LocalPlayer player) {
        println("Player moving: " + player.isMoving());
        return random.nextLong(1500,3000);
    }

    public BotState getBotState() {
        return botState;
    }

    public void setBotState(BotState botState) {
        this.botState = botState;
    }

    public boolean isSomeBool() {
        return someBool;
    }

    public void setSomeBool(boolean someBool) {
        this.someBool = someBool;
    }
}