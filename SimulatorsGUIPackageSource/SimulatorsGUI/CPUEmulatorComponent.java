/********************************************************************************
 * The contents of this file are subject to the GNU General Public License      *
 * (GPL) Version 2 or later (the "License"); you may not use this file except   *
 * in compliance with the License. You may obtain a copy of the License at      *
 * http://www.gnu.org/copyleft/gpl.html                                         *
 *                                                                              *
 * Software distributed under the License is distributed on an "AS IS" basis,   *
 * without warranty of any kind, either expressed or implied. See the License   *
 * for the specific language governing rights and limitations under the         *
 * License.                                                                     *
 *                                                                              *
 * This file was originally developed as part of the software suite that        *
 * supports the book "The Elements of Computing Systems" by Nisan and Schocken, *
 * MIT Press 2005. If you modify the contents of this file, please document and *
 * mark your changes clearly, for the benefit of others.                        *
 ********************************************************************************/

package SimulatorsGUI;

import HackGUI.*;
import Hack.CPUEmulator.*;
import Hack.ComputerParts.*;
import java.awt.*;
import java.io.*;

/**
 * This class represents the gui of the CPUEmulator.
 */
public abstract class CPUEmulatorComponent extends HackSimulatorComponent implements CPUEmulatorGUI  {

    // The dimension of this window.
    protected static final int EMULATOR_WIDTH = 1018;
    protected static final int EMULATOR_HEIGHT = 611;

    protected int numCores = 1;

    // The screen of the CPUEmulator.
    private ScreenComponent screen;

    // The keyboard of the CPUEmulator.
    private KeyboardComponent keyboard;

    // The memory of the CPUEmulator.
    private PointedMemoryComponent ram;

    // The ROM of the CPUEmulator.
    private ROMComponent rom;

    /**
     * Constructs a new CPUEmulatorComponent.
     */
    public CPUEmulatorComponent() {
        screen = new ScreenComponent();
        keyboard = new KeyboardComponent();
        ram = new PointedMemoryComponent();
        ram.setName("RAM");
        rom = new ROMComponent();
        rom.setName("ROM");

        // Sets the top level location of RAM and ROM.
        ram.setTopLevelLocation(this);
        rom.setTopLevelLocation(this);
    }

    public void setWorkingDir(File file) {
        rom.setWorkingDir(file);
    }

    public void loadProgram() {
        rom.loadProgram();
    }

    public Point getAdditionalDisplayLocation() {
        return new Point(476, 25);
    }

    /**
     * Returns the screen GUI component.
     */
    public ScreenGUI getScreen() {
        return screen;
    }

    /**
     * Returns the keyboard GUI component.
     */
    public KeyboardGUI getKeyboard() {
        return keyboard;
    }

    /**
     * Returns the RAM GUI component.
     */
    public PointedMemoryGUI getRAM() {
        return ram;
    }

    public int getNumCores() {
        return numCores;
    }

    /**
     * Returns the ROM GUI component.
     */
    public ROMGUI getROM() {
        return rom;
    }

    // Initialization of this component.
    protected void jbInit() {
        this.setLayout(null);
        screen.setToolTipText("Screen");
        screen.setBounds(new Rectangle(476, 25, screen.getWidth(), screen.getHeight()));
        keyboard.setBounds(new Rectangle(476, 285, keyboard.getWidth(), keyboard.getHeight()));
        ram.setVisibleRows(29);
        ram.setBounds(new Rectangle(264, 25, ram.getWidth(), ram.getHeight()));
        rom.setVisibleRows(29);
        rom.setBounds(new Rectangle(20, 25, rom.getWidth(), rom.getHeight()));

        this.add(ram, null);
        this.add(screen, null);
        this.add(rom, null);
        this.add(keyboard, null);
        setSize(EMULATOR_WIDTH, EMULATOR_HEIGHT);
    }
}
