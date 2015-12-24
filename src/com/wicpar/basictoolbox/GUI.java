package com.wicpar.basictoolbox;

import com.wicpar.sinkingsimulatorclassic.Main;
import com.wicpar.sinkingsimulatorclassic.*;
import com.wicpar.sinkingsimulatorclassic.Spring;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * Created by Frederic on 23/12/2015 at 18:42.
 */
public class GUI
{

	public JFrame mainframe = new JFrame("Toolbox");
	public ButtonGroup buttons;
	public ButtonGroup ships;

	public GUI()
	{
		JPanel tools = new JPanel();

		buttons = new ButtonGroup();

		JToggleButton pierce = new JToggleButton("Pierce");
		pierce.setActionCommand("Pierce");
		JToggleButton move = new JToggleButton("Move");
		move.setActionCommand("Move");
		JToggleButton spawn = new JToggleButton("Spawn");
		spawn.setActionCommand("Spawn");
		JToggleButton delete = new JToggleButton("Delete");
		delete.setActionCommand("Delete");

		buttons.add(move);
		buttons.add(pierce);
		buttons.add(spawn);
		buttons.add(delete);
		move.setSelected(true);

		Box box = Box.createVerticalBox();
		box.add(move);
		box.add(Box.createVerticalStrut(10));
		box.add(pierce);
		box.add(Box.createVerticalStrut(10));
		box.add(spawn);
		box.add(Box.createVerticalStrut(10));
		box.add(delete);
		tools.add(box);

		JPanel params = new JPanel();
		Box b = Box.createVerticalBox();
		b.add(makeSlider(-1000, 0, 1, "Sea Floor Height", new Value()
		{
			@Override
			public int getValue()
			{
				return (int) Main.ClassicSinkingSim.getInstance().getGround().h;
			}

			@Override
			public void setValue(double value)
			{
				Main.ClassicSinkingSim.getInstance().getGround().h = value;
			}
		}));
		b.add(makeSlider(-100, 1000, 0.01, "Water weight", new Value()
		{
			@Override
			public int getValue()
			{
				return (int) Shipsel.waterMassMul;
			}

			@Override
			public void setValue(double value)
			{
				Shipsel.waterMassMul = value;
			}
		}));
		b.add(makeSlider(0, 100, 0.1, "Buoyancy", new Value()
		{
			@Override
			public int getValue()
			{
				return (int) Sea.buoyancyMul;
			}

			@Override
			public void setValue(double value)
			{
				Sea.buoyancyMul = value;
			}
		}));

		b.add(makeSlider(0, 100, 1, "Flood Speed", new Value()
		{
			@Override
			public int getValue()
			{
				return (int) Ship.fluidmul;
			}

			@Override
			public void setValue(double value)
			{
				Ship.fluidmul = (float) value;
			}
		}));
		b.add(makeSlider(0, 500, 100, "Strength", new Value()
		{
			@Override
			public int getValue()
			{
				return (int) Spring.resmul;
			}

			@Override
			public void setValue(double value)
			{
				Spring.resmul = value;
			}
		}));
		b.add(makeSlider(0, 400, 1, "Rigidity", new Value()
		{
			@Override
			public int getValue()
			{
				return (int) Spring.strengthmul;
			}

			@Override
			public void setValue(double value)
			{
				Spring.strengthmul = value;
			}
		}));
		params.add(b);

		JPanel shippane = new JPanel();
		ships = new ButtonGroup();
		java.util.List<String> avaliableShips = ShipBuffer.getAvaliableShips();
		for (String name : avaliableShips)
		{
			JToggleButton button = new JToggleButton(name);
			button.setActionCommand(name);
			shippane.add(button);
			ships.add(button);
		}
		((JToggleButton) shippane.getComponent(0)).setSelected(true);
		mainframe.setContentPane(new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tools, new JScrollPane(params)), shippane));
		mainframe.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		mainframe.pack();
		mainframe.setVisible(true);
		mainframe.setSize(300, 600);
		shippane.setPreferredSize(shippane.getParent().getPreferredSize());
	}

	public Box makeSlider(int min, int max, double step, String Name, Value val)
	{
		Box box = Box.createHorizontalBox();
		box.add(new Label(Name));
		JSlider slider = new JSlider(min, max, (int) (val.getValue() / step));
		slider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent changeEvent)
			{
				JSlider source = (JSlider) changeEvent.getSource();
				val.setValue(source.getValue() * step);
			}
		});
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		box.add(slider);
		return box;
	}

	public interface Value
	{
		int getValue();

		void setValue(double value);
	}
}
