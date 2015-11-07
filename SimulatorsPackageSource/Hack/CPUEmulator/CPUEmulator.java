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

package Hack.CPUEmulator;

import Hack.Controller.*;
import Hack.ComputerParts.*;
import Hack.Events.*;
import Hack.Utilities.*;

/**
 * A CPU Emulator. Emulates machine code (In HACK format).
 *
 * Recognizes the following variables:
 * A - the address register (short)
 * D - the data register (short)
 * PC - the program counter (short)
 * RAM[i] - the contents of the RAM at location i (short)
 * ROM[i] - the contents of the ROM at location i (short)
 * time - the time that passed since the program started running (long) - READ ONLY
 *
 * Recognizes the following commands:
 * load <HACK file name> - loads the given file into the ROM
 * TickTock - advances the clock by one time unit (executes one instruction)
 */
public class CPUEmulator extends HackSimulator implements ComputerPartErrorEventListener {


    // Variables
    private static final String VAR_A = "A";
    private static final String VAR_D = "D";
    private static final String VAR_PC = "PC";
    private static final String VAR_RAM = "RAM";
    private static final String VAR_ROM = "ROM";
    private static final String VAR_TIME = "time";

    // Commands
    private static final String COMMAND_TICKTOCK = "ticktock";
    private static final String COMMAND_ROMLOAD = "load";
    private static final String COMMAND_SETVAR = "set";

    // The number of cores
    private int numCores;

    // The simulating cpu
    private CPU[] cpu;

    // The GUI of the CPUEmulator
    private CPUEmulatorGUI gui;

    // The list of recognized variables.
    private String[] vars;

    // The keyboard
    private Keyboard keyboard;

    // The current animation mode
    private int animationMode;

    /**
     * Constructs a new CPU Emulator with no GUI component.
     */
    public CPUEmulator( int numCores ) {

        this.numCores = numCores;

        /* Resources shared among cores */
        RAM ram = new RAM(null, null, null);
        ram.reset();

        ROM rom = new ROM(null);
        rom.reset();

        keyboard = new Keyboard(ram, null);
        keyboard.reset();

        /* Create each core */
        cpu = new CPU[numCores];
        for( int i = 0; i < numCores; i++ ) {

            PointerAddressRegisterAdapter A = new PointerAddressRegisterAdapter(null, ram);
            A.reset();

            Register D = new Register(null);
            D.reset();

            PointerAddressRegisterAdapter PC = new PointerAddressRegisterAdapter(null, rom);
            PC.reset();

            ALU alu = new ALU(null);
            alu.reset();

            Bus bus = new Bus(null);
            bus.reset();

            cpu[i] = new CPU(ram, rom, A, D, PC, alu, bus);
        }

        init();
    }

    /**
     * Constructs a new CPU Emulator with the given GUI component.
     */
    public CPUEmulator( CPUEmulatorGUI gui ) {
        this.gui = gui;
        this.numCores = gui.getNumCores();

        /* shared resources */
        RAM ram = new RAM(gui.getRAM(), null, gui.getScreen());
        ram.addErrorListener(this);
        ram.reset();

        ROM rom = new ROM(gui.getROM());
        rom.addErrorListener(this);
        rom.addProgramListener(this); // listens to program file change
        rom.reset();

        keyboard = new Keyboard(ram, gui.getKeyboard());
        keyboard.reset();

        /* Create each core */
        cpu = new CPU[numCores];
        for( int i = 0; i < numCores; i++ ) {

            PointerAddressRegisterAdapter A = new PointerAddressRegisterAdapter(gui.getA(i), ram);
            A.addErrorListener(this);
            A.reset();

            Register D = new Register(gui.getD(i));
            D.addErrorListener(this);
            D.reset();

            PointerAddressRegisterAdapter PC = new PointerAddressRegisterAdapter(gui.getPC(i), rom);
            PC.addErrorListener(this);
            PC.reset();

            ALU alu = new ALU(gui.getALU(i));
            alu.reset();

            Bus bus = new Bus(gui.getBus(i));
            bus.reset();

            cpu[i] = new CPU(ram, rom, A, D, PC, alu, bus);

        }

        init();
    }

    // Initializes the emulator
    private void init() {
        vars = new String[]{VAR_A, VAR_D, VAR_PC, VAR_RAM + "[]", VAR_ROM + "[]", VAR_TIME};
    }

    public String getName() {
        return "CPU Emulator";
    }


    private boolean isSharedResource( String varName ) {
        return varName.startsWith(VAR_A) || varName.startsWith(VAR_D) || varName.startsWith(VAR_PC);
    }

    private int getCoreFromSharedResource( String varName ) throws VariableException {
        int i = 0;
        if(isSharedResource(varName)){
            i = Integer.parseInt(varName.substring(varName.length() - 1, varName.length()));
            if( i < 0 || i >= numCores )
            {
                throw new VariableException("Unknown variable", varName);
            }
        }
        return i;
    }

    /**
     * Returns the value of the given variable.
     * Throws VariableException if the variable is not legal.
     */
    public String getValue(String varName) throws VariableException {

        try {

            /* Get core number */
            int i = getCoreFromSharedResource( varName );

            /* Core attributes */
            if (varName.startsWith(VAR_A)) {
                return String.valueOf(cpu[i].getA().get());
            }
            else if (varName.startsWith(VAR_D)) {
                return String.valueOf(cpu[i].getD().get());
            }
            else if (varName.startsWith(VAR_PC)) {
                return String.valueOf(cpu[i].getPC().get());
            }

            /* Shared attributes use core 0 to get values */
            else if (varName.equals(VAR_TIME))
                return String.valueOf(cpu[i].getTime());
            else if (varName.startsWith(VAR_RAM + "[")) {
                short index = getRamIndex(varName);
                return String.valueOf(cpu[i].getRAM().getValueAt(index));
            }
            else if (varName.startsWith(VAR_ROM + "[")) {
                short index = getRomIndex(varName);
                return String.valueOf(cpu[i].getROM().getValueAt(index));
            }
            else
                throw new VariableException("Unknown variable", varName);
        }
        catch ( NumberFormatException e )
        {
            throw new VariableException("Unknown variable", varName);
        }
    }

    /**
     * Sets the given variable with the given value.
     * Throws VariableException if the variable name or value are not legal.
     */
    public void setValue(String varName, String value) throws VariableException {
        int numValue;
        int i = getCoreFromSharedResource(varName);

        try {
            value = Conversions.toDecimalForm(value);

            if (varName.startsWith(VAR_A)) {
                numValue = Integer.parseInt(value);
                check_ram_address(varName, numValue);
                cpu[i].getA().store((short)numValue);
            }
            else if (varName.startsWith(VAR_D)) {
                numValue = Integer.parseInt(value);
                check_value(varName, numValue);
                cpu[i].getD().store((short)numValue);
            }
            else if (varName.startsWith(VAR_PC)) {
                numValue = Integer.parseInt(value);
                check_rom_address(varName, numValue);
                cpu[i].getPC().store((short)numValue);
            }
            else if (varName.equals(VAR_TIME))
                throw new VariableException("Read Only variable", varName);
            else if (varName.startsWith(VAR_RAM + "[")) {
                short index = getRamIndex(varName);
                numValue = Integer.parseInt(value);
                check_value(varName, numValue);
                cpu[i].getRAM().setValueAt(index, (short)numValue, false);
            }
            else if (varName.startsWith(VAR_ROM + "[")) {
                short index = getRomIndex(varName);
                numValue = Integer.parseInt(value);
                check_value(varName, numValue);
                cpu[i].getROM().setValueAt(index, (short)numValue, false);
            }
            else
                throw new VariableException("Unknown variable", varName);
        } catch (NumberFormatException nfe) {
            throw new VariableException("'" + value + "' is not a legal value for variable",
                                        varName);
        }
    }

    /**
     * Executes the given simulator command (given in args[] style).
     * Throws CommandException if the command is not legal.
     * Throws ProgramException if an error occurs in the program.
     */
    public void doCommand(String[] command)
     throws CommandException, ProgramException, VariableException {
        if (command.length == 0)
            throw new CommandException("Empty command", command);

        // hide gui highlights
        if (animationMode != HackController.NO_DISPLAY_CHANGES)
            hideHighlightes();

        // execute the appropriate command
        if (command[0].equals(COMMAND_TICKTOCK)) {
            if (command.length != 1)
                throw new CommandException("Illegal number of arguments to command", command);

            for( int i = 0; i < numCores; i++ )
                cpu[i].executeInstruction();
        }
        else if (command[0].equals(COMMAND_SETVAR)) {
            if (command.length != 3)
                throw new CommandException("Illegal number of arguments to command", command);
            setValue(command[1], command[2]);
        }
        else if (command[0].equals(COMMAND_ROMLOAD)) {
            if (command.length != 2)
                throw new CommandException("Illegal number of arguments to command", command);

            String fileName = workingDir.getAbsolutePath() + "/" + command[1];
            cpu[0].getROM().loadProgram(fileName);
            int oldAnimationMode = animationMode;
            setAnimationMode(HackController.DISPLAY_CHANGES);
            for( int i = 0; i < numCores; i++ ) {
                cpu[i].initProgram();
            }
            setAnimationMode(oldAnimationMode);
        }
        else
            throw new CommandException("Unknown simulator command", command);
    }

    // Hides all highlights in GUIs.
    private void hideHighlightes() {
        cpu[0].getRAM().hideHighlight();
        cpu[0].getROM().hideHighlight();
        for (int i = 0; i < numCores; i++ ) {
            cpu[i].getA().hideHighlight();
            cpu[i].getD().hideHighlight();
            cpu[i].getPC().hideHighlight();
            cpu[i].getALU().hideHighlight();
        }
    }

    /**
     * Restarts the CPUEmulator - program will be restarted.
     */
    public void restart() {
        for (int i=0; i < numCores; i++ ) { cpu[i].initProgram(); };
    }

    public void setAnimationMode(int newAnimationMode) {
        if (gui != null) {
            // enter NO_DISPLAY_CHANGES
            if (newAnimationMode == HackController.NO_DISPLAY_CHANGES &&
                    animationMode != HackController.NO_DISPLAY_CHANGES) {
                cpu[0].getRAM().disableUserInput();
                cpu[0].getROM().disableUserInput();

                for (int i = 0; i < numCores; i++) {
                    cpu[i].getA().disableUserInput();
                    cpu[i].getD().disableUserInput();
                    cpu[i].getPC().disableUserInput();
                }

                ScreenGUI screen = gui.getScreen();
                if (screen != null)
                    screen.startAnimation();
            }

            // exit NO_DISPLAY_CHANGES
            if (newAnimationMode != HackController.NO_DISPLAY_CHANGES &&
                    animationMode == HackController.NO_DISPLAY_CHANGES) {
                cpu[0].getRAM().enableUserInput();
                cpu[0].getROM().enableUserInput();

                for (int i = 0; i < numCores; i++) {
                    cpu[i].getA().enableUserInput();
                    cpu[i].getD().enableUserInput();
                    cpu[i].getPC().enableUserInput();
                }

                ScreenGUI screen = gui.getScreen();
                if (screen != null)
                    screen.stopAnimation();
            }
        }

        animationMode = newAnimationMode;

        boolean animate = (animationMode == HackController.ANIMATION);
        cpu[0].getRAM().setAnimate(animate);
        cpu[0].getROM().setAnimate(animate);

        for (int i = 0; i < numCores; i++) {
            cpu[i].getBus().setAnimate(animate);
            cpu[i].getA().setAnimate(animate);
            cpu[i].getD().setAnimate(animate);
            cpu[i].getPC().setAnimate(animate);
            cpu[i].getALU().setAnimate(animate);
        }

        boolean displayChanges = (animationMode != HackController.NO_DISPLAY_CHANGES);
        cpu[0].getRAM().setDisplayChanges(displayChanges);
        cpu[0].getROM().setDisplayChanges(displayChanges);

        for (int i = 0; i < numCores; i++) {
            cpu[i].getA().setDisplayChanges(displayChanges);
            cpu[i].getD().setDisplayChanges(displayChanges);
            cpu[i].getPC().setDisplayChanges(displayChanges);
            cpu[i].getALU().setDisplayChanges(displayChanges);
        }
    }

    public void setNumericFormat(int formatCode) {
        cpu[0].getRAM().setNumericFormat(formatCode);
        for (int i = 0; i < numCores; i++) {
            cpu[i].getA().setNumericFormat(formatCode);
            cpu[i].getD().setNumericFormat(formatCode);
            cpu[i].getPC().setNumericFormat(formatCode);
            cpu[i].getALU().setNumericFormat(formatCode);
        }
    }

    public void setAnimationSpeed(int speedUnit) {
        for (int i = 0; i < numCores; i++) {
            cpu[i].getBus().setAnimationSpeed(speedUnit);
        }
    }

    public int getInitialAnimationMode() {
        return HackController.DISPLAY_CHANGES;
    }

    public int getInitialNumericFormat() {
        return HackController.DECIMAL_FORMAT;
    }

    public void refresh() {
        cpu[0].getRAM().refreshGUI();
        cpu[0].getROM().refreshGUI();

        for (int i = 0; i < numCores; i++) {
            cpu[0].getBus().refreshGUI();
            cpu[0].getA().refreshGUI();
            cpu[0].getD().refreshGUI();
            cpu[0].getPC().refreshGUI();
            cpu[0].getALU().refreshGUI();
        }

        ScreenGUI screen = gui.getScreen();
        if (screen != null)
            screen.refresh();

    }

    public void prepareFastForward() {
        gui.requestFocus();
        keyboard.requestFocus();
    }

    public void prepareGUI() {
    }

    public String[] getVariables() {
        return vars;
    }

    protected HackSimulatorGUI getGUI() {
        return gui;
    }

    /**
     * Called when the ROM's current program is changed.
     * The event contains the source object, event type and the new program's file name (if any).
     */
    public void programChanged(ProgramEvent event) {
        super.programChanged(event);

        if (event.getType() == ProgramEvent.LOAD) {
            int oldAnimationMode = animationMode;
            setAnimationMode(HackController.DISPLAY_CHANGES);

            refresh();
            notifyListeners(ControllerEvent.ENABLE_MOVEMENT, null);

            for (int i = 0; i < numCores; i++) {
                cpu[i].initProgram();
            }

            setAnimationMode(oldAnimationMode);
        }
    }

    /**
     * Called when an error occured in a computer part.
     * The event contains the source computer part and the error message.
     */
    public void computerPartErrorOccured(ComputerPartErrorEvent event) {
        displayMessage(event.getErrorMessage(), true);
    }

    // receives a variable name of the form xxx[i] and returns the numeric
    // value of i, which is an address in the RAM.
    // Throws VariableException if i is not a legal address in the RAM.
    private static short getRamIndex(String varName) throws VariableException {
        if (varName.indexOf("]") == -1)
            throw new VariableException("Missing ']'", varName);

        String indexStr = varName.substring(varName.indexOf("[") + 1, varName.indexOf("]"));
        int index = Integer.parseInt(indexStr);
        if (index < 0 || index >= Definitions.RAM_SIZE)
            throw new VariableException("Illegal variable index", varName);

        return (short)index;
    }

    // receives a variable name of the form xxx[i] and returns the numeric
    // value of i, which is an address in the ROM.
    // Throws VariableException if i is not a legal address in the ROM.
    private static short getRomIndex(String varName) throws VariableException {
        if (varName.indexOf("]") == -1)
            throw new VariableException("Missing ']'", varName);

        String indexStr = varName.substring(varName.indexOf("[") + 1, varName.indexOf("]"));
        int index = Integer.parseInt(indexStr);
        if (index < 0 || index >= Definitions.ROM_SIZE)
            throw new VariableException("Illegal variable index", varName);

        return (short)index;
    }

    // Checks that the given value is a legal 16-bit value
    private void check_value(String varName, int value) throws VariableException {
        if (value < -32768 || value >= 32768)
            throw new VariableException(value +
                " is an illegal value for variable", varName);
    }

    // Checks that the given value is a legal 16-bit address
    private void check_ram_address(String varName, int value) throws VariableException {
        if (value < 0 || value >= Definitions.RAM_SIZE)
            throw new VariableException(value +
                " is an illegal value for", varName);
    }

    // Checks that the given value is a legal 16-bit address
    private void check_rom_address(String varName, int value) throws VariableException {
        if (value < 0 || value >= Definitions.ROM_SIZE)
            throw new VariableException(value +
                " is an illegal value for", varName);
    }
}
