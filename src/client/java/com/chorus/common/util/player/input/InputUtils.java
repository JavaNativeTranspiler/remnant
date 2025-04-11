/**
 * Created: 12/11/2024
 */

package com.chorus.common.util.player.input;

import chorus0.asm.accessors.MinecraftClientAccessor;
import chorus0.asm.accessors.MouseHandlerAccessor;
import com.chorus.common.QuickImports;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InputUtils implements QuickImports {
    private static final HashMap<Integer, Boolean> mouseButtons = new HashMap<>();
    private static final ExecutorService clickExecutor = Executors.newFixedThreadPool(100);
    private static final ExecutorService keyExecutor = Executors.newFixedThreadPool(100);
    /**
     * Retrieves the mouse handler object from the Minecraft client.
     *
     * @return The mouse handler object.
     */
    public static MouseHandlerAccessor getMouseHandler() {
        return (MouseHandlerAccessor) ((MinecraftClientAccessor) MinecraftClient.getInstance()).getMouse();
    }

    /**
     * Simulates a mouse button click.
     *
     * @param keyCode The key code of the mouse button to simulate (e.g., GLFW.GLFW_MOUSE_BUTTON_LEFT).
     */
    public static void simulateClick(int keyCode) {
        simulateClick(keyCode, 35);
    }

    /**
     * Simulates a mouse click by pressing and releasing the specified button.
     *
     * @param keyCode The key code of the mouse button to simulate (e.g., GLFW.GLFW_MOUSE_BUTTON_LEFT).
     * @param millis  The duration in milliseconds to hold the button down.
     */
    public static void simulateClick(int keyCode, int millis) {
        clickExecutor.submit(() -> {
            try {
                simulatePress(keyCode);
                Thread.sleep(millis);
                simulateRelease(keyCode);
            } catch (InterruptedException ignored) {
                ignored.printStackTrace();
            }
        });
    }
    /**
     * Simulates a key press by pressing and releasing the specified key.
     *
     * @param key The key code of the key to simulate.
     * @param millis  The duration in milliseconds to hold the key down.
     */
    public static void simulateKeyPress(KeyBinding key, int millis) {
        keyExecutor.submit(() -> {
            try {
                key.setPressed(true);
                Thread.sleep(millis);
                key.setPressed(false);
            } catch (InterruptedException ignored) {
                ignored.printStackTrace();
            }
        });
    }

    /**
     * Simulates a mouse button press.
     *
     * @param keyCode The key code of the mouse button to simulate (e.g., GLFW.GLFW_MOUSE_BUTTON_LEFT).
     */
    public static void simulatePress(int keyCode) {
        simulateMouseEvent(keyCode, GLFW.GLFW_PRESS);
    }

    /**
     * Simulates a mouse button release.
     *
     * @param keyCode The key code of the mouse button to simulate (e.g., GLFW.GLFW_MOUSE_BUTTON_LEFT).
     */
    public static void simulateRelease(int keyCode) {
        simulateMouseEvent(keyCode, GLFW.GLFW_RELEASE);
    }

    /**
     * Internal method to simulate mouse button events (click or release).
     *
     * @param keyCode The key code of the mouse button.
     * @param action  The action (GLFW.GLFW_PRESS or GLFW.GLFW_RELEASE).
     */
    private static void simulateMouseEvent(int keyCode, int action) {
        MouseHandlerAccessor mouseHandler = getMouseHandler();
        if (mouseHandler != null) {
            mouseHandler.press(MinecraftClient.getInstance().getWindow().getHandle(), keyCode, action, 0);
            if (action == GLFW.GLFW_PRESS) {
                mouseButtons.put(keyCode, true);
            } else if (action == GLFW.GLFW_RELEASE) {
                mouseButtons.put(keyCode, false);
            }
        }
    }

    /**
     * Checks if a specific mouse button is currently pressed based on internal tracking.
     *
     * @param keyCode The key code of the mouse button to check.
     * @return true if the mouse button is pressed according to internal tracking, false otherwise.
     */
    public static boolean isMouseButtonPressed(int keyCode) {
        return mouseButtons.getOrDefault(keyCode, false);
    }

    /**
     * Internal method to check if a specific mouse button is currently pressed using GLFW.
     *
     * @param button The key code of the mouse button to check.
     * @return true if the mouse button is pressed, false otherwise.
     */
    public static boolean mouseDown(int button) {
        return GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), button) == GLFW.GLFW_PRESS;
    }

    /**
     * Internal method to check keyboard state.
     *
     * @param key The key code of the keyboard button.
     * @return true if the keyboard button is pressed, false otherwise.
     */
    public static boolean keyDown(int key) {
        return GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), key) == GLFW.GLFW_PRESS;
    }

    public static String getKeyName(int keyCode) {
        return switch (keyCode) {
            case GLFW.GLFW_MOUSE_BUTTON_2 -> "RMB";
            case GLFW.GLFW_MOUSE_BUTTON_3 -> "MMB";
            case GLFW.GLFW_KEY_UNKNOWN -> "None";
            case GLFW.GLFW_KEY_ESCAPE -> "Esc";
            case GLFW.GLFW_KEY_GRAVE_ACCENT -> "Grave Accent";
            case GLFW.GLFW_KEY_WORLD_1 -> "World 1";
            case GLFW.GLFW_KEY_WORLD_2 -> "World 2";
            case GLFW.GLFW_KEY_PRINT_SCREEN -> "Print Screen";
            case GLFW.GLFW_KEY_PAUSE -> "Pause";
            case GLFW.GLFW_KEY_INSERT -> "Insert";
            case GLFW.GLFW_KEY_DELETE -> "Delete";
            case GLFW.GLFW_KEY_HOME -> "Home";
            case GLFW.GLFW_KEY_PAGE_UP -> "Page Up";
            case GLFW.GLFW_KEY_PAGE_DOWN -> "Page Down";
            case GLFW.GLFW_KEY_END -> "End";
            case GLFW.GLFW_KEY_TAB -> "Tab";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "Left Control";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "Right Control";
            case GLFW.GLFW_KEY_LEFT_ALT -> "Left Alt";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "Right Alt";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "Left Shift";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "Right Shift";
            case GLFW.GLFW_KEY_UP -> "Arrow Up";
            case GLFW.GLFW_KEY_DOWN -> "Arrow Down";
            case GLFW.GLFW_KEY_LEFT -> "Arrow Left";
            case GLFW.GLFW_KEY_RIGHT -> "Arrow Right";
            case GLFW.GLFW_KEY_APOSTROPHE -> "Apostrophe";
            case GLFW.GLFW_KEY_BACKSPACE -> "Backspace";
            case GLFW.GLFW_KEY_CAPS_LOCK -> "Caps Lock";
            case GLFW.GLFW_KEY_MENU -> "Menu";
            case GLFW.GLFW_KEY_LEFT_SUPER -> "Left Super";
            case GLFW.GLFW_KEY_RIGHT_SUPER -> "Right Super";
            case GLFW.GLFW_KEY_ENTER -> "Enter";
            case GLFW.GLFW_KEY_KP_ENTER -> "Numpad Enter";
            case GLFW.GLFW_KEY_NUM_LOCK -> "Num Lock";
            case GLFW.GLFW_KEY_SPACE -> "Space";
            case GLFW.GLFW_KEY_F1, GLFW.GLFW_KEY_F2, GLFW.GLFW_KEY_F3, GLFW.GLFW_KEY_F4,
                 GLFW.GLFW_KEY_F5, GLFW.GLFW_KEY_F6, GLFW.GLFW_KEY_F7, GLFW.GLFW_KEY_F8,
                 GLFW.GLFW_KEY_F9, GLFW.GLFW_KEY_F10, GLFW.GLFW_KEY_F11, GLFW.GLFW_KEY_F12,
                 GLFW.GLFW_KEY_F13, GLFW.GLFW_KEY_F14, GLFW.GLFW_KEY_F15, GLFW.GLFW_KEY_F16,
                 GLFW.GLFW_KEY_F17, GLFW.GLFW_KEY_F18, GLFW.GLFW_KEY_F19, GLFW.GLFW_KEY_F20,
                 GLFW.GLFW_KEY_F21, GLFW.GLFW_KEY_F22, GLFW.GLFW_KEY_F23, GLFW.GLFW_KEY_F24,
                 GLFW.GLFW_KEY_F25 -> "F" + (keyCode - GLFW.GLFW_KEY_F1 + 1);
            default -> {
                String keyName = GLFW.glfwGetKeyName(keyCode, 0);
                if (keyName == null) {
                    yield "None";
                }
                yield Character.toUpperCase(keyName.charAt(0)) + keyName.substring(1).toLowerCase();
            }
        };
    }

}