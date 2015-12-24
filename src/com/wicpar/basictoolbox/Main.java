package com.wicpar.basictoolbox;

import com.wicpar.sinkingsimulatorclassic.*;
import com.wicpar.wicparbase.input.GenericGLFW;
import com.wicpar.wicparbase.input.IInput;
import com.wicpar.wicparbase.mech.Base;
import com.wicpar.wicparbase.physics.IDynamical;
import com.wicpar.wicparbase.physics.system.Physical;
import com.wicpar.wicparbase.utils.plugins.Injector;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.Extension;
import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginWrapper;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;

import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;

/**
 * Created by Frederic on 25/09/2015 at 13:58.
 */
public class Main extends Plugin
{
	/**
	 * Constructor to be used by plugin manager for plugin instantiation.
	 * Your plugins have to provide constructor with this exact signature to
	 * be successfully loaded by manager.
	 *
	 * @param wrapper
	 */
	public Main(PluginWrapper wrapper)
	{
		super(wrapper);
	}

	@Extension
	public static class testPlugin implements Injector, IDynamical
	{

		Logger logger = LoggerFactory.getLogger(this.getClass());
		private GUI gui;
		private Vector2d LastWPos;
		private boolean movedown, move, justMoved;
		private Vector2d lastCursorPos = new Vector2d();

		@Override
		public void OnHandlerPreInit()
		{
		}

		@Override
		public void OnHandlerPostInit()
		{

		}

		@Override
		public void OnGamePreInit()
		{
			com.wicpar.sinkingsimulatorclassic.Main.ClassicSinkingSim.keepDebugControl = false;
		}

		@Override
		public void OnGamePostInit()
		{
			gui = new GUI();
			Base.getInputHandler().addInput(new IInput()
			{
				@Override
				public boolean Invoke(int i, Object[] objects)
				{
					Camera cam = com.wicpar.sinkingsimulatorclassic.Main.ClassicSinkingSim.getInstance().getCam();
					Sea sea = com.wicpar.sinkingsimulatorclassic.Main.ClassicSinkingSim.getInstance().getSea();
					if (i == GenericGLFW.onCursorPosCallback)
					{
						double x = (Double) objects[1];
						double y = (Double) objects[2];

						if (movedown || move)
						{
							cam.Translate(x - lastCursorPos.x, lastCursorPos.y - y);
						}
						lastCursorPos.set(x, y);
					} else if (i == GenericGLFW.onMouseButtonCallback)
					{
						int button = (Integer) objects[1];
						int action = (Integer) objects[2];
						final DoubleBuffer x = BufferUtils.createDoubleBuffer(1);
						final DoubleBuffer y = BufferUtils.createDoubleBuffer(1);
						final IntBuffer w = BufferUtils.createIntBuffer(1);
						final IntBuffer h = BufferUtils.createIntBuffer(1);
						glfwGetCursorPos((Long) objects[0], x, y);
						double xp = x.get(0);
						double yp = y.get(0);
						x.rewind();
						y.rewind();
						glfwGetWindowSize((Long) objects[0], w, h);
						w.rewind();
						h.rewind();
						xp /= w.get(0);
						yp /= h.get(0);
						xp = xp * 2 - 1;
						yp = -yp * 2 + 1;
						xp = cam.untransformX(xp);
						yp = cam.untransformY(yp);
						if (button == 0 && action == 1)
						{
							String command = gui.buttons.getSelection().getActionCommand();
							if (command != null)
							{
								if (command.equals("Move"))
								{
									movedown = true;
								} else if (command.equals("Spawn"))
									ShipBuffer.ScheduleShip(gui.ships.getSelection().getActionCommand(), new Vector3d(xp, yp, 0));
								else if (command.equals("Pierce"))
								{
									final double[] buf = new double[2];
									buf[0] = xp;
									buf[1] = yp;
									Base.getClassHandler().UpdateClass((o, objects1) -> {
										Shipsel s = ((Shipsel) o);
										if (s.getPos().distance(buf[0], buf[1], 0) < 1)
											s.dispose();
									}, Shipsel.class);
								} else if (command.equals("Delete"))
								{
									final double[] buf = new double[2];
									final LinkedList<Ship> todel = new LinkedList<>();
									buf[0] = xp;
									buf[1] = yp;
									Base.getClassHandler().UpdateClass((o, objects1) -> {
										Shipsel s = ((Shipsel) o);
										if (s.getPos().distance(buf[0], buf[1], 0) < 1)
											todel.add(s.getParent());
									}, Shipsel.class);
									Base.getClassHandler().removeClass(todel.toArray());
									todel.clear();
								}
							}
						}
						if (button == 0 && action == 0)
						{
							String command = gui.buttons.getSelection().getActionCommand();
							if (command == null)
							{
							} else if (command.equals("Move"))
							{
								movedown = false;
							}
						}
						if (button == 1 && action == 1)
						{
							move = true;
						}
						if (button == 1 && action == 0)
						{
							move = false;
						}
					} else if (i == GenericGLFW.onWindowSizeCallback)
					{
						int x, y;
						IntBuffer vp = BufferUtils.createIntBuffer(4);
						GL11.glGetIntegerv(GL11.GL_VIEWPORT, vp);
						GL11.glViewport(0, 0, x = (Integer) objects[1], y = (Integer) objects[2]);
						cam.Translate(-(x - vp.get(2)) / 2., (y - vp.get(3)) / 2.);
						cam.UpdateViewPort(x, y);
						sea.setDivisions(x / 2);

					} else if (i == GenericGLFW.onWindowPosCallback)
					{
						if (LastWPos == null)
						{
							LastWPos = new Vector2d((Integer) objects[1], (Integer) objects[2]);
						} else
						{
							int x, y;
							cam.Translate((LastWPos.x - (x = (Integer) objects[1])), -(LastWPos.y - (y = (Integer) objects[2])));
							LastWPos.set(x, y);
						}
					} else if (i == GenericGLFW.onKeyCallback)
					{
						if (((Integer) objects[1]) == GLFW.GLFW_KEY_ESCAPE && ((Integer) objects[3]) != 0)
						{
							gui.mainframe.setVisible(!gui.mainframe.isVisible());
						} else if (((Integer) objects[1]) == GLFW.GLFW_KEY_P)
						{
							logger.info(" fps: " + 1 / Base.getDelta() + " Dynamicals Num: " + Stats.getClassCount(Physical.class));
						}
					} else if (i == GenericGLFW.onScrollCallback)
					{
						cam.Scale(Math.pow(1.1, (Double) objects[2]));
					}
					return true;
				}
			});
			Base.getClassHandler().addClass(this);
		}

		@Override
		public void OnGameFinish()
		{
			gui.mainframe.dispose();
		}

		@Override
		public void dispose()
		{

		}

		@Override
		public boolean isDisposed()
		{
			return false;
		}

		@Override
		public void UpdateForces(double v)
		{

		}
	}

}
