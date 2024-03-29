/*
 * ported to 0.37b7
 * ported to v0.36
 */
package arcadeflex.v037b7.vidhrdw;

//common imports
import static arcadeflex.common.ptrLib.*;
import static arcadeflex.common.libc.expressions.*;
import static arcadeflex.common.libc.cstring.*;
//generic imports
import static arcadeflex.v037b7.generic.funcPtr.*;
//mame imports
import static arcadeflex.v037b7.mame.mame.*;
import static arcadeflex.v037b7.mame.drawgfx.*;
import static arcadeflex.v037b7.mame.drawgfxH.*;
import static arcadeflex.v037b7.mame.osdependH.*;
//vidhrdw imports
import static arcadeflex.v037b7.vidhrdw.generic.*;

public class frogger {

    static int flipscreen;

    public static UBytePtr frogger_attributesram = new UBytePtr();

    /* Cocktail mode implemented by Chad Hendrickson, July 26, 1999           */
    /**
     * *************************************************************************
     *
     * Convert the color PROMs into a more useable format.
     *
     * Frogger has one 32 bytes palette PROM, connected to the RGB output this
     * way:
     *
     * bit 7 -- 220 ohm resistor -- BLUE -- 470 ohm resistor -- BLUE -- 220 ohm
     * resistor -- GREEN -- 470 ohm resistor -- GREEN -- 1 kohm resistor --
     * GREEN -- 220 ohm resistor -- RED -- 470 ohm resistor -- RED bit 0 -- 1
     * kohm resistor -- RED
     *
     * Additionally, there is a bit which is 1 in the upper half of the display
     * (136 lines? I'm not sure of the exact value); it is connected to blue
     * through a 470 ohm resistor. It is used to make the river blue instead of
     * black.
     *
     **************************************************************************
     */
    public static VhConvertColorPromPtr frogger_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int i;

            for (i = 0; i < 32; i++) {
                int bit0, bit1, bit2;

                bit0 = (color_prom.read(i) >> 0) & 0x01;
                bit1 = (color_prom.read(i) >> 1) & 0x01;
                bit2 = (color_prom.read(i) >> 2) & 0x01;
                palette[3 * i] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                bit0 = (color_prom.read(i) >> 3) & 0x01;
                bit1 = (color_prom.read(i) >> 4) & 0x01;
                bit2 = (color_prom.read(i) >> 5) & 0x01;
                palette[3 * i + 1] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
                bit0 = 0;
                bit1 = (color_prom.read(i) >> 6) & 0x01;
                bit2 = (color_prom.read(i) >> 7) & 0x01;
                palette[3 * i + 2] = ((char) (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2));
            }

            /* use an otherwise unused pen for the river background */
            palette[3 * 4] = ((char) (0));
            palette[3 * 4 + 1] = ((char) (0));
            palette[3 * 4 + 2] = ((char) (0x47));

            /* normal */
            for (i = 0; i < 4 * 8; i++) {
                if ((i & 3) != 0) {
                    colortable[i] = (char) i;
                } else {
                    colortable[i] = 0;
                }
            }
            /* blue background (river) */
            for (i = 4 * 8; i < 4 * 16; i++) {
                if ((i & 3) != 0) {
                    colortable[i] = (char) (i - 4 * 8);
                } else {
                    colortable[i] = 4;
                }
            }
        }
    };

    public static WriteHandlerPtr frogger_attributes_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (((offset & 1) != 0) && frogger_attributesram.read(offset) != data) {
                int i;

                for (i = offset / 2; i < videoram_size[0]; i += 32) {
                    dirtybuffer[i] = 1;
                }
            }

            frogger_attributesram.write(offset, data);
        }
    };

    public static WriteHandlerPtr frogger_flipscreen_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            if (flipscreen != (data & 1)) {
                flipscreen = data & 1;
                memset(dirtybuffer, 1, videoram_size[0]);
            }
        }
    };

    /**
     * *************************************************************************
     *
     * Draw the game screen in the given osd_bitmap. Do NOT call
     * osd_update_display() from this function, it will be called by the main
     * emulation engine.
     *
     **************************************************************************
     */
    public static VhUpdatePtr frogger_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int i, offs;

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx, sy, col;

                    dirtybuffer[offs] = 0;

                    sx = offs % 32;
                    sy = offs / 32;
                    col = frogger_attributesram.read(2 * sx + 1) & 7;
                    col = ((col >> 1) & 0x03) | ((col << 2) & 0x04);

                    if (flipscreen != 0) {
                        sx = 31 - sx;
                        sy = 31 - sy;
                        drawgfx(tmpbitmap, Machine.gfx[0],
                                videoram.read(offs),
                                col + (sx >= 16 ? 8 : 0), /* blue background in the lower 128 lines */
                                flipscreen, flipscreen, 8 * sx, 8 * sy,
                                null, TRANSPARENCY_NONE, 0);
                    } else {
                        drawgfx(tmpbitmap, Machine.gfx[0],
                                videoram.read(offs),
                                col + (sx <= 15 ? 8 : 0), /* blue background in the upper 128 lines */
                                flipscreen, flipscreen, 8 * sx, 8 * sy,
                                null, TRANSPARENCY_NONE, 0);
                    }
                }
            }

            /* copy the temporary bitmap to the screen */
            {
                int scroll[] = new int[32];
                int s;

                for (i = 0; i < 32; i++) {
                    s = frogger_attributesram.read(2 * i);
                    if (flipscreen != 0) {
                        scroll[31 - i] = (((s << 4) & 0xf0) | ((s >> 4) & 0x0f));
                    } else {
                        scroll[i] = -(((s << 4) & 0xf0) | ((s >> 4) & 0x0f));
                    }
                }

                copyscrollbitmap(bitmap, tmpbitmap, 0, null, 32, scroll, Machine.visible_area, TRANSPARENCY_NONE, 0);
            }

            /* Draw the sprites. Note that it is important to draw them exactly in this */
 /* order, to have the correct priorities. */
            for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
                if (spriteram.read(offs + 3) != 0) {
                    int x, y, col;

                    x = spriteram.read(offs + 3);
                    y = spriteram.read(offs);
                    y = ((y << 4) & 0xf0) | ((y >> 4) & 0x0f);
                    col = spriteram.read(offs + 2) & 7;
                    col = ((col >> 1) & 0x03) | ((col << 2) & 0x04);

                    if (flipscreen != 0) {
                        x = 242 - x;
                        y = 240 - y;
                        drawgfx(bitmap, Machine.gfx[1],
                                spriteram.read(offs + 1) & 0x3f,
                                col,
                                NOT(spriteram.read(offs + 1) & 0x40), NOT(spriteram.read(offs + 1) & 0x80),
                                x, 30 * 8 - y,
                                Machine.visible_area, TRANSPARENCY_PEN, 0);
                    } else {
                        drawgfx(bitmap, Machine.gfx[1],
                                spriteram.read(offs + 1) & 0x3f,
                                col,
                                spriteram.read(offs + 1) & 0x40, spriteram.read(offs + 1) & 0x80,
                                x, 30 * 8 - y,
                                Machine.visible_area, TRANSPARENCY_PEN, 0);
                    }
                }
            }
        }
    };

    /* the alternate version doesn't have the sprite & scroll registers mangling, */
 /* but it still has the color code mangling. */
    public static VhUpdatePtr frogger2_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int i, offs;

            /* for every character in the Video RAM, check if it has been modified */
 /* since last time and update it accordingly. */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                if (dirtybuffer[offs] != 0) {
                    int sx, sy, col;

                    dirtybuffer[offs] = 0;

                    sx = offs % 32;
                    sy = offs / 32;
                    col = frogger_attributesram.read(2 * sx + 1) & 7;
                    col = ((col >> 1) & 0x03) | ((col << 2) & 0x04);

                    if (flipscreen != 0) {
                        sx = 31 - sx;
                        sy = 31 - sy;
                        drawgfx(tmpbitmap, Machine.gfx[0],
                                videoram.read(offs),
                                col + (sx >= 16 ? 8 : 0), /* blue background in the lower 128 lines */
                                flipscreen, flipscreen, 8 * sx, 8 * sy,
                                null, TRANSPARENCY_NONE, 0);
                    } else {
                        drawgfx(tmpbitmap, Machine.gfx[0],
                                videoram.read(offs),
                                col + (sx <= 15 ? 8 : 0), /* blue background in the upper 128 lines */
                                flipscreen, flipscreen, 8 * sx, 8 * sy,
                                null, TRANSPARENCY_NONE, 0);
                    }
                }
            }

            /* copy the temporary bitmap to the screen */
            {
                int scroll[] = new int[32];

                for (i = 0; i < 32; i++) {
                    if (flipscreen != 0) {
                        scroll[31 - i] = frogger_attributesram.read(2 * i);
                    } else {
                        scroll[i] = -frogger_attributesram.read(2 * i);
                    }
                }

                copyscrollbitmap(bitmap, tmpbitmap, 0, null, 32, scroll, Machine.visible_area, TRANSPARENCY_NONE, 0);
            }

            /* Draw the sprites. Note that it is important to draw them exactly in this */
 /* order, to have the correct priorities. */
            for (offs = spriteram_size[0] - 4; offs >= 0; offs -= 4) {
                if (spriteram.read(offs + 3) != 0) {
                    int x, y, col;

                    x = spriteram.read(offs + 3);
                    y = spriteram.read(offs);
                    col = spriteram.read(offs + 2) & 7;
                    col = ((col >> 1) & 0x03) | ((col << 2) & 0x04);

                    if (flipscreen != 0) {
                        x = 242 - x;
                        y = 240 - y;
                        drawgfx(bitmap, Machine.gfx[1],
                                spriteram.read(offs + 1) & 0x3f,
                                col,
                                NOT(spriteram.read(offs + 1) & 0x40), NOT(spriteram.read(offs + 1) & 0x80),
                                x, 30 * 8 - y,
                                Machine.visible_area, TRANSPARENCY_PEN, 0);
                    } else {
                        drawgfx(bitmap, Machine.gfx[1],
                                spriteram.read(offs + 1) & 0x3f,
                                col,
                                spriteram.read(offs + 1) & 0x40, spriteram.read(offs + 1) & 0x80,
                                x, 30 * 8 - y,
                                Machine.visible_area, TRANSPARENCY_PEN, 0);
                    }
                }
            }
        }
    };
}
