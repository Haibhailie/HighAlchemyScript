package net.botwithus;

import net.botwithus.api.game.hud.inventories.Backpack;
import net.botwithus.api.game.hud.inventories.Bank;

import net.botwithus.internal.scripts.ScriptDefinition;
import net.botwithus.rs3.events.impl.ChatMessageEvent;
import net.botwithus.rs3.events.impl.ServerTickedEvent;
import net.botwithus.rs3.game.Area;
import net.botwithus.rs3.game.Client;
import net.botwithus.rs3.game.Coordinate;
import net.botwithus.rs3.game.Item;
import net.botwithus.rs3.game.hud.interfaces.Component;
import net.botwithus.rs3.game.queries.builders.components.ComponentQuery;
import net.botwithus.rs3.game.queries.builders.items.InventoryItemQuery;
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery;
import net.botwithus.rs3.game.queries.results.ResultSet;
import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer;
import net.botwithus.rs3.game.scene.entities.object.SceneObject;
import net.botwithus.rs3.game.skills.Skills;
import net.botwithus.rs3.script.Execution;
import net.botwithus.rs3.script.LoopingScript;
import net.botwithus.rs3.script.config.ScriptConfig;
import net.botwithus.rs3.util.Regex;

import java.util.Random;
import java.util.regex.Pattern;

public class HighAlchemyScript extends LoopingScript {

    private BotState botState = BotState.IDLE;
    private boolean someBool = true;
    private Random random = new Random();
    private Area GE = new Area.Rectangular(new Coordinate(3148,3507,0), new Coordinate(3180,3472,0));

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
        Execution.delay(random.nextLong(1000,3000));
        LocalPlayer player = Client.getLocalPlayer();
        if (player == null || Client.getGameState() != Client.GameState.LOGGED_IN || botState == BotState.IDLE) {
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
                //do some code that handles your skilling
                Execution.delay(handleSkilling(player));
            }
            case BANKING -> {
                //handle your banking logic, etc
                Execution.delay(handleBanking(player));
            }
        }
    }
    /*
    *
    * private long handleBanking(LocalPlayer player)
        {
            println("War Unlock state" + someBool);
            println("Player moving 1:" +player.isMoving());

            if(player.isMoving())
            {
                return random.nextLong(3000,5000);
            }
            if (Bank.isOpen())
            {
                println("Bank is open");
                Bank.depositAllExcept(54004);
                botState = BotState.SKILLING;
                return random.nextLong(1000,3000);
            }
            if (player.getCoordinate().getRegionId() != 13105)
            {
                WalkToAlKharid(player);
            }
            else
            {
                ResultSet<SceneObject> banks = SceneObjectQuery.newQuery().name("Bank booth").option("Bank").inside(AlKharid).results();
                if (banks.isEmpty())
                {
                    println("Bank query was empty.");
                }
                else
                {
                    SceneObject bank = banks.random();
                    if (bank != null) {
                        println("Yay, we found our bank.");
                        println("Interacted bank: " + bank.interact("Bank"));
                        Bank.depositAllExcept(54004);
                    }
                }
            }

            return random.nextLong(1500,3000);
        }
    * */
    private long handleBanking(LocalPlayer player) {

        //println("Anim id: " + player.getAnimationId());
        println("Player moving: " + player.isMoving());
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
            ResultSet<SceneObject> banks = SceneObjectQuery.newQuery().name("Bank booth").option("Bank").inside(GE).results();
            if (banks.isEmpty())
            {
                println("Bank query was empty.");
                botState = BotState.IDLE;
            }
            else
            {
                SceneObject bank = banks.random();
                if (bank != null) {
                    botState = BotState.BANKING;
                    println("Yay, we found our bank.");
                    println("Interacted bank: " + bank.interact("Bank"));
                    Bank.loadLastPreset();
                }
            }
        }

        botState = BotState.ALCHING;
        return random.nextLong(1500,3000);
    }

    private long handleSkilling(LocalPlayer player) {
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