/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package arcadeflex.v037b7.vidhrdw;

//common imports
import static arcadeflex.common.ptrLib.*;
import static arcadeflex.common.libc.cstring.*;
//generic imports
import static arcadeflex.v037b7.generic.funcPtr.*;
//mame imports
import static arcadeflex.v037b7.mame.mame.*;
import static arcadeflex.v037b7.mame.common.*;
import static arcadeflex.v037b7.mame.drawgfx.*;
import static arcadeflex.v037b7.mame.drawgfxH.*;
import static arcadeflex.v037b7.mame.osdependH.*;
import static arcadeflex.v037b7.mame.palette.*;
import static arcadeflex.v037b7.mame.paletteH.*;
//platform imports
import static arcadeflex.v037b7.platform.osdepend.*;
//vidhrdw imports
import static arcadeflex.v037b7.vidhrdw.generic.*;

public class tehkanwc {

    public static UBytePtr tehkanwc_videoram1 = new UBytePtr();
    public static int[] tehkanwc_videoram1_size = new int[1];
    static osd_bitmap tmpbitmap1 = null;
    static char[] dirtybuffer1;
    static /*unsigned*/ char[] scroll_x = new char[2];
    static /*unsigned*/ char scroll_y;
    static /*unsigned*/ char led0, led1;

    public static VhStartPtr tehkanwc_vh_start = new VhStartPtr() {
        public int handler() {
            if (generic_vh_start.handler() != 0) {
                return 1;
            }

            if ((tmpbitmap1 = bitmap_alloc(2 * Machine.drv.screen_width, Machine.drv.screen_height)) == null) {
                generic_vh_stop.handler();
                return 1;
            }

            if ((dirtybuffer1 = new char[tehkanwc_videoram1_size[0]]) == null) {
                bitmap_free(tmpbitmap1);
                generic_vh_stop.handler();
                return 1;
            }
            memset(dirtybuffer1, 1, tehkanwc_videoram1_size[0]);

            return 0;
        }
    };

    public static VhStopPtr tehkanwc_vh_stop = new VhStopPtr() {
        public void handler() {
            dirtybuffer1 = null;
            bitmap_free(tmpbitmap1);
            generic_vh_stop.handler();
        }
    };

    public static ReadHandlerPtr tehkanwc_videoram1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return tehkanwc_videoram1.read(offset);
        }
    };

    public static WriteHandlerPtr tehkanwc_videoram1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            tehkanwc_videoram1.write(offset, data);
            dirtybuffer1[offset] = 1;
        }
    };

    public static ReadHandlerPtr tehkanwc_scroll_x_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return scroll_x[offset] & 0xFF;
        }
    };

    public static ReadHandlerPtr tehkanwc_scroll_y_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return scroll_y & 0xFF;
        }
    };

    public static WriteHandlerPtr tehkanwc_scroll_x_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            scroll_x[offset] = (char) (data & 0xFF);
        }
    };

    public static WriteHandlerPtr tehkanwc_scroll_y_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            scroll_y = (char) (data & 0xFF);
        }
    };

    public static WriteHandlerPtr gridiron_led0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            led0 = (char) (data & 0xFF);
        }
    };
    public static WriteHandlerPtr gridiron_led1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            led1 = (char) (data & 0xFF);
        }
    };

    /*
	   Gridiron Fight has a LED display on the control panel, to let each player
	   choose the formation without letting the other know.
	   We emulate it by showing a character on the corner of the screen; the
	   association between the bits of the port and the led segments is:
	
	    ---0---
	   |       |
	   5       1
	   |       |
	    ---6---
	   |       |
	   4       2
	   |       |
	    ---3---
	
	   bit 7 = enable (0 = display off)
     */
    static int ledvalues[]
            = {0x86, 0xdb, 0xcf, 0xe6, 0xed, 0xfd, 0x87, 0xff, 0xf3, 0xf1};

    static void gridiron_drawled(osd_bitmap bitmap,/*unsigned*/ char led, int player) {
        int i;

        if ((led & 0x80) == 0) {
            return;
        }

        for (i = 0; i < 10; i++) {
            if (led == ledvalues[i]) {
                break;
            }
        }

        if (i < 10) {
            if (player == 0) {
                drawgfx(bitmap, Machine.gfx[0],
                        0xc0 + i,
                        0x0a,
                        0, 0,
                        0, 232,
                        Machine.visible_area, TRANSPARENCY_NONE, 0);
            } else {
                drawgfx(bitmap, Machine.gfx[0],
                        0xc0 + i,
                        0x03,
                        1, 1,
                        0, 16,
                        Machine.visible_area, TRANSPARENCY_NONE, 0);
            }
        } else {
            logerror("unknown LED %02x for player %d\n", led, player);
        }
    }

    public static VhUpdatePtr tehkanwc_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs;

            palette_init_used_colors();

            {
                int color, code, i;
                int[] colmask = new int[16];
                int pal_base;

                pal_base = Machine.drv.gfxdecodeinfo[2].color_codes_start;

                for (color = 0; color < 16; color++) {
                    colmask[color] = 0;
                }

                for (offs = tehkanwc_videoram1_size[0] - 2; offs >= 0; offs -= 2) {
                    code = tehkanwc_videoram1.read(offs) + ((tehkanwc_videoram1.read(offs + 1) & 0x30) << 4);
                    color = tehkanwc_videoram1.read(offs + 1) & 0x0f;

                    colmask[color] |= Machine.gfx[2].pen_usage[code];
                }

                for (color = 0; color < 16; color++) {
                    for (i = 0; i < 16; i++) {
                        if ((colmask[color] & (1 << i)) != 0) {
                            palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
                        }
                    }
                }

                pal_base = Machine.drv.gfxdecodeinfo[1].color_codes_start;

                for (color = 0; color < 16; color++) {
                    colmask[color] = 0;
                }

                for (offs = 0; offs < spriteram_size[0]; offs += 4) {
                    code = spriteram.read(offs + 0) + ((spriteram.read(offs + 1) & 0x08) << 5);
                    color = spriteram.read(offs + 1) & 0x07;

                    colmask[color] |= Machine.gfx[1].pen_usage[code];
                }

                for (color = 0; color < 16; color++) {
                    if ((colmask[color] & (1 << 0)) != 0) {
                        palette_used_colors.write(pal_base + 16 * color, PALETTE_COLOR_TRANSPARENT);
                    }
                    for (i = 1; i < 16; i++) {
                        if ((colmask[color] & (1 << i)) != 0) {
                            palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
                        }
                    }
                }

                pal_base = Machine.drv.gfxdecodeinfo[0].color_codes_start;

                for (color = 0; color < 16; color++) {
                    colmask[color] = 0;
                }

                for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                    code = videoram.read(offs) + ((colorram.read(offs) & 0x10) << 4);
                    color = colorram.read(offs) & 0x0f;
                    colmask[color] |= Machine.gfx[0].pen_usage[code];
                }

                for (color = 0; color < 16; color++) {
                    if ((colmask[color] & (1 << 0)) != 0) {
                        palette_used_colors.write(pal_base + 16 * color, PALETTE_COLOR_TRANSPARENT);
                    }
                    for (i = 1; i < 16; i++) {
                        if ((colmask[color] & (1 << i)) != 0) {
                            palette_used_colors.write(pal_base + 16 * color + i, PALETTE_COLOR_USED);
                        }
                    }
                }
            }

            if (palette_recalc() != null) {
                memset(dirtybuffer, 1, videoram_size[0]);
                memset(dirtybuffer1, 1, tehkanwc_videoram1_size[0]);
            }

            /* draw the background */
            for (offs = tehkanwc_videoram1_size[0] - 2; offs >= 0; offs -= 2) {
                if (dirtybuffer1[offs] != 0 || dirtybuffer1[offs + 1] != 0) {
                    int sx, sy;

                    dirtybuffer1[offs] = dirtybuffer1[offs + 1] = 0;

                    sx = offs % 64;
                    sy = offs / 64;

                    drawgfx(tmpbitmap1, Machine.gfx[2],
                            tehkanwc_videoram1.read(offs) + ((tehkanwc_videoram1.read(offs + 1) & 0x30) << 4),
                            tehkanwc_videoram1.read(offs + 1) & 0x0f,
                            tehkanwc_videoram1.read(offs + 1) & 0x40, tehkanwc_videoram1.read(offs + 1) & 0x80,
                            sx * 8, sy * 8,
                            null, TRANSPARENCY_NONE, 0);
                }
            }
            {
                int scrolly = -scroll_y;
                int scrollx = -(scroll_x[0] + 256 * scroll_x[1]);
                copyscrollbitmap(bitmap, tmpbitmap1, 1, new int[]{scrollx}, 1, new int[]{scrolly}, Machine.visible_area, TRANSPARENCY_NONE, 0);
            }

            /* draw the foreground chars which don't have priority over sprites */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                int sx, sy;

                dirtybuffer[offs] = 0;

                sx = offs % 32;
                sy = offs / 32;

                if (((colorram.read(offs) & 0x20)) != 0) {
                    drawgfx(bitmap, Machine.gfx[0],
                            videoram.read(offs) + ((colorram.read(offs) & 0x10) << 4),
                            colorram.read(offs) & 0x0f,
                            colorram.read(offs) & 0x40, colorram.read(offs) & 0x80,
                            sx * 8, sy * 8,
                            Machine.visible_area, TRANSPARENCY_PEN, 0);
                }
            }

            /* draw sprites */
            for (offs = 0; offs < spriteram_size[0]; offs += 4) {
                drawgfx(bitmap, Machine.gfx[1],
                        spriteram.read(offs + 0) + ((spriteram.read(offs + 1) & 0x08) << 5),
                        spriteram.read(offs + 1) & 0x07,
                        spriteram.read(offs + 1) & 0x40, spriteram.read(offs + 1) & 0x80,
                        spriteram.read(offs + 2) + ((spriteram.read(offs + 1) & 0x20) << 3) - 0x80, spriteram.read(offs + 3),
                        Machine.visible_area, TRANSPARENCY_PEN, 0);
            }

            /* draw the foreground chars which have priority over sprites */
            for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                int sx, sy;

                dirtybuffer[offs] = 0;

                sx = offs % 32;
                sy = offs / 32;

                if ((colorram.read(offs) & 0x20) == 0) {
                    drawgfx(bitmap, Machine.gfx[0],
                            videoram.read(offs) + ((colorram.read(offs) & 0x10) << 4),
                            colorram.read(offs) & 0x0f,
                            colorram.read(offs) & 0x40, colorram.read(offs) & 0x80,
                            sx * 8, sy * 8,
                            Machine.visible_area, TRANSPARENCY_PEN, 0);
                }
            }

            gridiron_drawled(bitmap, led0, 0);
            gridiron_drawled(bitmap, led1, 1);
        }
    };
}
