package com.github.kamppix.twodminecwaft.commands;

import javax.swing.*;

import com.github.kamppix.twodminecwaft.world.World;

public class SeedInputBox {
	
	static JTextField seedInput;
    
    public World getSeededWorld() {

    	seedInput = new JTextField();
        JOptionPane.showMessageDialog(null, seedInput, "Insert Seed", JOptionPane.PLAIN_MESSAGE);
        if (seedInput.getText().isEmpty()) {
        	return new World(1234567890);
        }
        return new World(seedInput.getText().hashCode());
    }
}
