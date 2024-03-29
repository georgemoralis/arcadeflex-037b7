/*
 * ported to v0.37b7
 *
 */
package arcadeflex.v037b7.vidhrdw;

//common imports
import static arcadeflex.common.ptrLib.*;
//generic imports
import static arcadeflex.v037b7.generic.funcPtr.*;
//mame imports
import static arcadeflex.v037b7.mame.common.*;
import static arcadeflex.v037b7.mame.commonH.*;
import static arcadeflex.v037b7.mame.mame.*;
import static arcadeflex.v037b7.mame.drawgfx.*;
import static arcadeflex.v037b7.mame.drawgfxH.*;
import static arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v037b7.mame.osdependH.*;
//vidhrdw imports
import static arcadeflex.v037b7.vidhrdw.generic.*;

public class _1943 {

    public static UBytePtr c1943_scrollx = new UBytePtr();
    public static UBytePtr c1943_scrolly = new UBytePtr();
    public static UBytePtr c1943_bgscrolly = new UBytePtr();
    static int chon, objon, sc1on, sc2on;
    static int flipscreen;

    static osd_bitmap sc2bitmap;
    static osd_bitmap sc1bitmap;
    static /*unsigned char*/ char[][][] sc2map = new char[9][8][2];
    static /*unsigned char*/ char[][][] sc1map = new char[9][9][2];

    /**
     * *************************************************************************
     * Convert the color PROMs into a more useable format.
     * <p>
     * 1943 has three 256x4 palette PROMs (one per gun) and a lot ;-) of 256x4
     * lookup table PROMs. The palette PROMs are connected to the RGB output
     * this way:
     * <p>
     * bit 3 -- 220 ohm resistor -- RED/GREEN/BLUE -- 470 ohm resistor --
     * RED/GREEN/BLUE -- 1 kohm resistor -- RED/GREEN/BLUE bit 0 -- 2.2kohm
     * resistor -- RED/GREEN/BLUE
     * *************************************************************************
     */
    static int TOTAL_COLORS(int gfxn) {
        return Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity;
    }

    public static VhConvertColorPromPtr c1943_vh_convert_color_prom = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            int p_inc = 0;
            for (int i = 0; i < Machine.drv.total_colors; i++) {
                int bit0, bit1, bit2, bit3;

                bit0 = (color_prom.read(0) >> 0) & 0x01;
                bit1 = (color_prom.read(0) >> 1) & 0x01;
                bit2 = (color_prom.read(0) >> 2) & 0x01;
                bit3 = (color_prom.read(0) >> 3) & 0x01;
                palette[p_inc++] = ((char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));
                bit0 = (color_prom.read(Machine.drv.total_colors) >> 0) & 0x01;
                bit1 = (color_prom.read(Machine.drv.total_colors) >> 1) & 0x01;
                bit2 = (color_prom.read(Machine.drv.total_colors) >> 2) & 0x01;
                bit3 = (color_prom.read(Machine.drv.total_colors) >> 3) & 0x01;
                palette[p_inc++] = ((char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));
                bit0 = (color_prom.read(2 * Machine.drv.total_colors) >> 0) & 0x01;
                bit1 = (color_prom.read(2 * Machine.drv.total_colors) >> 1) & 0x01;
                bit2 = (color_prom.read(2 * Machine.drv.total_colors) >> 2) & 0x01;
                bit3 = (color_prom.read(2 * Machine.drv.total_colors) >> 3) & 0x01;
                palette[p_inc++] = ((char) (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3));

                color_prom.inc();
            }

            color_prom.inc(2 * Machine.drv.total_colors);
            /* color_prom now points to the beginning of the lookup table */

 /* characters use colors 64-79 */
            for (int i = 0; i < TOTAL_COLORS(0); i++) {
                colortable[Machine.drv.gfxdecodeinfo[0].color_codes_start + i] = (char) (color_prom.readinc() + 64);
            }

            color_prom.inc(128);
            /* skip the bottom half of the PROM - not used */

 /* foreground tiles use colors 0-63 */
            for (int i = 0; i < TOTAL_COLORS(1); i++) {
                /* color 0 MUST map to pen 0 in order for transparency to work */
                if (i % Machine.gfx[1].color_granularity == 0) {
                    colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = 0;
                } else {
                    colortable[Machine.drv.gfxdecodeinfo[1].color_codes_start + i] = (char) (color_prom.read(0) + 16 * (color_prom.read(256) & 0x03));
                }

                color_prom.inc();
            }
            color_prom.inc(TOTAL_COLORS(1));

            /* background tiles use colors 0-63 */
            for (int i = 0; i < TOTAL_COLORS(2); i++) {
                //COLOR(2,i) = color_prom[0] + 16 * (color_prom[256] & 0x03);
                colortable[Machine.drv.gfxdecodeinfo[2].color_codes_start + i] = (char) (color_prom.read(0) + 16 * (color_prom.read(256) & 0x03));
                color_prom.inc();
            }
            color_prom.inc(TOTAL_COLORS(2));

            /* sprites use colors 128-255 */
 /* bit 3 of BMPROM.07 selects priority over the background, but we handle */
 /* it differently for speed reasons */
            for (int i = 0; i < TOTAL_COLORS(3); i++) {
                //COLOR(3,i) = color_prom[0] + 16 * (color_prom[256] & 0x07) + 128;
                colortable[Machine.drv.gfxdecodeinfo[3].color_codes_start + i] = (char) (color_prom.read(0) + 16 * (color_prom.read(256) & 0x07) + 128);
                color_prom.inc();
            }
            color_prom.inc(TOTAL_COLORS(3));
        }
    };

    public static VhStartPtr c1943_vh_start = new VhStartPtr() {
        public int handler() {
            if ((sc2bitmap = bitmap_alloc(9 * 32, 8 * 32)) == null) {
                return 1;
            }

            if ((sc1bitmap = bitmap_alloc(9 * 32, 9 * 32)) == null) {
                bitmap_free(sc2bitmap);
                return 1;
            }

            if (generic_vh_start.handler() == 1) {
                bitmap_free(sc2bitmap);
                bitmap_free(sc1bitmap);
                return 1;
            }

            //memset (sc2map, 0xff, sizeof (sc2map));
            //memset (sc1map, 0xff, sizeof (sc1map));
            for (int i = 0; i < sc2map.length; i++) {
                for (int k = 0; k < sc2map[i].length; k++) {
                    for (int z = 0; z < sc2map[i][k].length; z++) {
                        sc2map[i][k][z] = 0xff;
                    }
                }
            }
            for (int i = 0; i < sc1map.length; i++) {
                for (int k = 0; k < sc1map[i].length; k++) {
                    for (int z = 0; z < sc1map[i][k].length; z++) {
                        sc1map[i][k][z] = 0xff;
                    }
                }
            }

            return 0;
        }
    };

    public static VhStopPtr c1943_vh_stop = new VhStopPtr() {
        public void handler() {
            bitmap_free(sc2bitmap);
            bitmap_free(sc1bitmap);
        }
    };

    public static WriteHandlerPtr c1943_c804_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int bankaddress;
            UBytePtr RAM = memory_region(REGION_CPU1);

            /* bits 0 and 1 are coin counters */
            coin_counter_w.handler(0, data & 1);
            coin_counter_w.handler(1, data & 2);

            /* bits 2, 3 and 4 select the ROM bank */
            bankaddress = 0x10000 + (data & 0x1c) * 0x1000;
            cpu_setbank(1, new UBytePtr(RAM, bankaddress));

            /* bit 5 resets the sound CPU - we ignore it */
 /* bit 6 flips screen */
            if (flipscreen != (data & 0x40)) {
                flipscreen = data & 0x40;
                //		memset(dirtybuffer,1,c1942_backgroundram_size);
            }

            /* bit 7 enables characters */
            chon = data & 0x80;
        }
    };

    public static WriteHandlerPtr c1943_d806_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* bit 4 enables bg 1 */
            sc1on = data & 0x10;

            /* bit 5 enables bg 2 */
            sc2on = data & 0x20;

            /* bit 6 enables sprites */
            objon = data & 0x40;
        }
    };

    /**
     * *************************************************************************
     * Draw the game screen in the given osd_bitmap. Do NOT call
     * osd_update_display() from this function, it will be called by the main
     * emulation engine.
     * *************************************************************************
     */
    public static VhUpdatePtr c1943_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            int offs, sx, sy;
            int bg_scrolly, bg_scrollx;
            UBytePtr p;
            int top, left, xscroll, yscroll;

            /* TODO: support flipscreen */
            if (sc2on != 0) {
                p = new UBytePtr(memory_region(REGION_GFX5), 0x8000);
                bg_scrolly = c1943_bgscrolly.read(0) + 256 * c1943_bgscrolly.read(1);
                offs = 16 * ((bg_scrolly >> 5) + 8);

                top = 8 - (bg_scrolly >> 5) % 9;

                bg_scrolly &= 0x1f;

                for (sy = 0; sy < 9; sy++) {
                    int ty = (sy + top) % 9;
                    UBytePtr map = new UBytePtr(sc2map[ty][0], 0);//unsigned char *map = &sc2map[ty][0][0];
                    offs &= 0x7fff;
                    /* Enforce limits (for top of scroll) */

                    for (sx = 0; sx < 8; sx++) {
                        int tile, attr, offset;
                        offset = offs + 2 * sx;

                        tile = p.read(offset);
                        attr = p.read(offset + 1);

                        if (tile != map.read(0) || attr != map.read(1)) {
                            map.write(0, tile);
                            map.write(1, attr);
                            drawgfx(sc2bitmap, Machine.gfx[2],
                                    tile,
                                    (attr & 0x3c) >> 2,
                                    attr & 0x40, attr & 0x80,
                                    (8 - ty) * 32, sx * 32,
                                    null,
                                    TRANSPARENCY_NONE, 0);
                        }
                        //map.offset += 2;
                    }
                    offs -= 0x10;
                }

                xscroll = (top * 32 - bg_scrolly);
                yscroll = 0;
                copyscrollbitmap(bitmap, sc2bitmap,
                        1, new int[]{xscroll},
                        1, new int[]{yscroll},
                        Machine.visible_area,
                        TRANSPARENCY_NONE, 0);
            } else {
                fillbitmap(bitmap, Machine.pens[0], Machine.visible_area);
            }

            if (objon != 0) {
                /* Draw the sprites which don't have priority over the foreground. */
                for (offs = spriteram_size[0] - 32; offs >= 0; offs -= 32) {
                    int color;

                    color = spriteram.read(offs + 1) & 0x0f;
                    if (color == 0x0a || color == 0x0b) /* the priority is actually selected by */ /* bit 3 of BMPROM.07 */ {
                        sx = spriteram.read(offs + 3) - ((spriteram.read(offs + 1) & 0x10) << 4);
                        sy = spriteram.read(offs + 2);
                        if (flipscreen != 0) {
                            sx = 240 - sx;
                            sy = 240 - sy;
                        }

                        drawgfx(bitmap, Machine.gfx[3],
                                spriteram.read(offs) + ((spriteram.read(offs + 1) & 0xe0) << 3),
                                color,
                                flipscreen, flipscreen,
                                sx, sy,
                                Machine.visible_area, TRANSPARENCY_PEN, 0);
                    }
                }
            }

            /* TODO: support flipscreen */
            if (sc1on != 0) {
                p = memory_region(REGION_GFX5);

                bg_scrolly = c1943_scrolly.read(0) + 256 * c1943_scrolly.read(1);
                bg_scrollx = c1943_scrollx.read(0);
                offs = 16 * ((bg_scrolly >> 5) + 8) + 2 * (bg_scrollx >> 5);
                if ((bg_scrollx & 0x80) != 0) {
                    offs -= 0x10;
                }

                top = 8 - (bg_scrolly >> 5) % 9;
                left = (bg_scrollx >> 5) % 9;

                bg_scrolly &= 0x1f;
                bg_scrollx &= 0x1f;

                for (sy = 0; sy < 9; sy++) {
                    int ty = (sy + top) % 9;
                    offs &= 0x7fff;
                    /* Enforce limits (for top of scroll) */

                    for (sx = 0; sx < 9; sx++) {
                        int tile, attr, offset;
                        int tx = (sx + left) % 9;
                        UBytePtr map = new UBytePtr(sc1map[ty][tx], 0);//unsigned char *map = &sc1map[ty][tx][0];
                        offset = offs + (sx * 2);

                        tile = p.read(offset);
                        attr = p.read(offset + 1);

                        if (tile != map.read(0) || attr != map.read(1)) {
                            map.write(0, tile);
                            map.write(1, attr);
                            tile += 256 * (attr & 0x01);
                            drawgfx(sc1bitmap, Machine.gfx[1],
                                    tile,
                                    (attr & 0x3c) >> 2,
                                    attr & 0x40, attr & 0x80,
                                    (8 - ty) * 32, tx * 32,
                                    null,
                                    TRANSPARENCY_NONE, 0);
                        }
                    }
                    offs -= 0x10;
                }

                xscroll = (top * 32 - bg_scrolly);
                yscroll = -(left * 32 + bg_scrollx);
                copyscrollbitmap(bitmap, sc1bitmap,
                        1, new int[]{xscroll},
                        1, new int[]{yscroll},
                        Machine.visible_area,
                        TRANSPARENCY_COLOR, 0);
            }

            if (objon != 0) {
                /* Draw the sprites which have priority over the foreground. */
                for (offs = spriteram_size[0] - 32; offs >= 0; offs -= 32) {
                    int color;

                    color = spriteram.read(offs + 1) & 0x0f;
                    if (color != 0x0a && color != 0x0b) /* the priority is actually selected by */ /* bit 3 of BMPROM.07 */ {
                        sx = spriteram.read(offs + 3) - ((spriteram.read(offs + 1) & 0x10) << 4);
                        sy = spriteram.read(offs + 2);
                        if (flipscreen != 0) {
                            sx = 240 - sx;
                            sy = 240 - sy;
                        }

                        drawgfx(bitmap, Machine.gfx[3],
                                spriteram.read(offs) + ((spriteram.read(offs + 1) & 0xe0) << 3),
                                color,
                                flipscreen, flipscreen,
                                sx, sy,
                                Machine.visible_area, TRANSPARENCY_PEN, 0);
                    }
                }
            }

            if (chon != 0) {
                /* draw the frontmost playfield. They are characters, but draw them as sprites */
                for (offs = videoram_size[0] - 1; offs >= 0; offs--) {
                    sx = offs % 32;
                    sy = offs / 32;
                    if (flipscreen != 0) {
                        sx = 31 - sx;
                        sy = 31 - sy;
                    }

                    drawgfx(bitmap, Machine.gfx[0],
                            videoram.read(offs) + ((colorram.read(offs) & 0xe0) << 3),
                            colorram.read(offs) & 0x1f,
                            flipscreen, flipscreen,
                            8 * sx, 8 * sy,
                            Machine.visible_area, TRANSPARENCY_COLOR, 79);
                }
            }
        }
    };
}
