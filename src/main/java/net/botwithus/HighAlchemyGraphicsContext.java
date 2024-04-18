package net.botwithus;

import net.botwithus.rs3.imgui.ImGui;
import net.botwithus.rs3.imgui.ImGuiWindowFlag;
import net.botwithus.rs3.script.ScriptConsole;
import net.botwithus.rs3.script.ScriptGraphicsContext;

public class HighAlchemyGraphicsContext extends ScriptGraphicsContext {

    private HighAlchemyScript script;

    public HighAlchemyGraphicsContext(ScriptConsole scriptConsole, HighAlchemyScript script) {
        super(scriptConsole);
        this.script = script;
    }

    @Override
    public void drawSettings() {
        if (ImGui.Begin("My script", ImGuiWindowFlag.None.getValue())) {
            if (ImGui.BeginTabBar("My bar", ImGuiWindowFlag.None.getValue())) {
                if (ImGui.BeginTabItem("Settings", ImGuiWindowFlag.None.getValue())) {
                    ImGui.Text("Auto High Level Alchemy bot.");
                    ImGui.Text("Script state is: " + script.getBotState());
                    ImGui.EndTabItem();
                }
                if (ImGui.Button("Start")) {
                    //button has been clicked
                    script.setBotState(HighAlchemyScript.BotState.BANKING);
                }
                ImGui.SameLine();
                if (ImGui.Button("Stop")) {
                    //has been clicked
                    script.setBotState(HighAlchemyScript.BotState.IDLE);
                }
                ImGui.EndTabItem();
            }
            if (ImGui.BeginTabItem("Stats", ImGuiWindowFlag.None.getValue())) {
                ImGui.Text("Runtime: " + script.formatTime(script.getRunTime()));
                ImGui.Text("Items Alched: " + script.getItemsAlched());
                ImGui.Text("Overall Profit: " + script.getProfit());
                ImGui.Text("Profit per hour: " + script.getProfitPerHour());
                ImGui.EndTabItem();
            }
            ImGui.EndTabBar();
            ImGui.End();
        }

    }

    @Override
    public void drawOverlay() {
        super.drawOverlay();
    }
}
