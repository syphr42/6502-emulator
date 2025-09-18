/*
 * Copyright © 2025 Gregory P. Moyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.syphr.emulator.cli.tui;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.component.message.ShellMessageBuilder;
import org.springframework.shell.component.view.TerminalUI;
import org.springframework.shell.component.view.TerminalUIBuilder;
import org.springframework.shell.component.view.control.AppView;
import org.springframework.shell.component.view.control.BoxView;
import org.springframework.shell.component.view.control.GridView;
import org.springframework.shell.component.view.control.MenuBarView;
import org.springframework.shell.component.view.control.MenuBarView.MenuBarItem;
import org.springframework.shell.component.view.control.MenuView.MenuItem;
import org.springframework.shell.component.view.control.MenuView.MenuItemCheckStyle;
import org.springframework.shell.component.view.control.StatusBarView;
import org.springframework.shell.component.view.control.StatusBarView.StatusItem;
import org.springframework.shell.component.view.control.View;
import org.springframework.shell.component.view.event.EventLoop;
import org.springframework.shell.component.view.event.KeyEvent;
import org.springframework.shell.geom.HorizontalAlign;
import org.springframework.shell.geom.VerticalAlign;
import org.syphr.emulator.cli.memory.MemoryMap;
import org.syphr.emulator.cpu.Address;
import org.syphr.emulator.cpu.Operation;
import org.syphr.emulator.cpu.Value;

import java.util.List;

import static org.syphr.emulator.cpu.AddressMode.*;
import static org.syphr.emulator.cpu.Operation.*;

@Command
@RequiredArgsConstructor
public class TUI
{
    private final TerminalUIBuilder uiBuilder;

    private TerminalUI ui;
    private EventLoop eventLoop;
    private AppView app;

    @Command(command = "tui", description = "Start a terminal interface")
    public void start()
    {
        ui = uiBuilder.build();
        eventLoop = ui.getEventLoop();
        app = buildInterface(eventLoop, ui);

        eventLoop.onDestroy(eventLoop.keyEvents().doOnNext(m -> {
            if (m.getPlainKey() == KeyEvent.Key.q && m.hasCtrl()) {
                requestQuit();
            }
        }).subscribe());

        ui.setRoot(app, true);
        // TODO ui.setFocus();
        ui.run();
    }

    private AppView buildInterface(EventLoop eventLoop, TerminalUI component)
    {
        // category selector on left, scenario selector on right
        GridView grid = new GridView();
        component.configure(grid);
        grid.setRowSize(0);
        grid.setColumnSize(0);

        grid.addItem(buildHexView(eventLoop), 0, 0, 1, 1, 0, 0);

        MenuBarView menuBar = buildMenuBar(eventLoop);
        StatusBarView statusBar = buildStatusBar(eventLoop);

        // we use main app view to represent scenario browser
        AppView app = new AppView(grid, menuBar, statusBar);
        component.configure(app);

        return app;
    }

    private View buildHexView(EventLoop eventLoop)
    {
        var root = new BoxView();
        root.setTitle("Hex Viewer");
        root.setShowBorder(true);

        MemoryMap memoryMap = buildMemoryMap();

        root.setDrawFunction((screen, rect) -> {
            screen.writerBuilder().build().text("Hello World", rect, HorizontalAlign.CENTER, VerticalAlign.CENTER);
            return rect;
        });

        return root;
    }

    private MemoryMap buildMemoryMap()
    {
        var programStart = Address.of(0x02FB);
        List<Operation> operations = List.of(lda(immediate(Value.ZERO)),
                                             beq(relative(Value.of(2))),
                                             inc(accumulator()),
                                             inc(accumulator()),
                                             nop(),
                                             jsr(absolute(Address.of(0x030A))),
                                             jmp(absolute(programStart)),
                                             nop(),
                                             nop(),
                                             inc(accumulator()),
                                             rts());

        return MemoryMap.of(programStart, operations);
    }

    private MenuBarView buildMenuBar(EventLoop eventLoop)
    {
        Runnable quitAction = this::requestQuit;
        MenuBarView menuBar = MenuBarView.of(MenuBarItem.of("File",
                                                            MenuItem.of("Quit",
                                                                        MenuItemCheckStyle.NOCHECK,
                                                                        quitAction))
                                                        .setHotKey(KeyEvent.Key.f |
                                                                   KeyEvent.KeyMask.AltMask));

        ui.configure(menuBar);
        return menuBar;
    }

    private StatusBarView buildStatusBar(EventLoop eventLoop)
    {
        Runnable quitAction = this::requestQuit;
        Runnable visibilityAction = () -> app.toggleStatusBarVisibility();
        StatusBarView statusBar = new StatusBarView(new StatusItem[] {StatusItem.of(
                "CTRL-Q Quit",
                quitAction), StatusItem.of("F10 Status Bar", visibilityAction, KeyEvent.Key.f10)});
        ui.configure(statusBar);
        return statusBar;
    }

    private void requestQuit()
    {
        eventLoop.dispatch(ShellMessageBuilder.ofInterrupt());
    }
}
