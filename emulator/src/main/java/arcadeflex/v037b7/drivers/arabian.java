/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package arcadeflex.v037b7.drivers;

//common imports
import static arcadeflex.common.ptrLib.*;
//generic imports
import static arcadeflex.v037b7.generic.funcPtr.*;
//mame imports
import static arcadeflex.v037b7.mame.inptport.*;
import static arcadeflex.v037b7.mame.inptportH.*;
import static arcadeflex.v037b7.mame.memoryH.*;
import static arcadeflex.v037b7.mame.driverH.*;
import static arcadeflex.v037b7.mame.common.*;
import static arcadeflex.v037b7.mame.commonH.*;
import static arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v037b7.mame.cpuintrfH.*;
import static arcadeflex.v037b7.mame.drawgfxH.*;
import static arcadeflex.v037b7.mame.sndintrfH.*;
//platform imports
import static arcadeflex.v037b7.platform.osdepend.*;
//sound imports
import static arcadeflex.v037b7.sound.ay8910.*;
import static arcadeflex.v037b7.sound.ay8910H.*;
//vidhrdw imports
import static arcadeflex.v037b7.vidhrdw.arabian.*;
import static arcadeflex.v037b7.vidhrdw.generic.*;

public class arabian {

    static /*UINT8*/ int u8_arabian_clock = 0;
    static /*UINT8*/ int u8_portB = 0;

    public static InterruptPtr arabian_interrupt = new InterruptPtr() {
        public int handler() {
            u8_arabian_clock = (u8_arabian_clock + 1) & 0xFF;
            return interrupt.handler();
        }
    };

    static int last;
    public static WriteHandlerPtr arabian_portB_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int pc;

            u8_portB = data & 0xFF;

            pc = cpu_get_pc();
            if ((pc == 0x0a7a) || (pc == 0x002a)) {
                pc = cpu_geturnpc();
            }
            if ((data & 0xec) != last) {
                logerror("Port B written  %02X  PC=%04X\n", data, pc);
            }
            last = data & 0xec;

            coin_counter_w.handler(0, ~data & 0x01);
            coin_counter_w.handler(1, ~data & 0x02);
        }
    };

    public static ReadHandlerPtr arabian_input_port_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int pom;

            if ((u8_portB & 0x10) != 0) /* if 1 read the switches */ {
                switch (offset) {
                    case 0:
                        pom = readinputport(2);
                        break;
                    case 1:
                        pom = readinputport(3);
                        break;
                    case 2:
                        pom = readinputport(4);
                        break;
                    case 3:
                        pom = readinputport(5);
                        break;
                    case 4:
                        pom = readinputport(6);
                        break;
                    case 5:
                        pom = readinputport(7);
                        break;
                    case 6:
                        pom = u8_arabian_clock >> 4;
                        break;
                    case 8:
                        pom = u8_arabian_clock & 0x0f;
                        break;
                    default:
                        logerror("Input Port %04X read.  PC=%04X\n", offset + 0xd7f0, cpu_get_pc());
                        pom = 0;
                        break;
                }
            } else /* if bit 4 of AY port 0f==0 then read RAM memory instead of switches */ {
                UBytePtr RAM = memory_region(REGION_CPU1);
                pom = RAM.read(0xd7f0 + offset);
            }

            return pom;
        }
    };

    static MemoryReadAddress readmem[] = {
        new MemoryReadAddress(0x0000, 0x7fff, MRA_ROM),
        new MemoryReadAddress(0xc000, 0xc000, input_port_0_r),
        new MemoryReadAddress(0xc200, 0xc200, input_port_1_r),
        new MemoryReadAddress(0xd000, 0xd7ef, MRA_RAM),
        new MemoryReadAddress(0xd7f0, 0xd7ff, arabian_input_port_r),
        new MemoryReadAddress(-1) /* end of table */};

    static MemoryWriteAddress writemem[] = {
        new MemoryWriteAddress(0x0000, 0x7fff, MWA_ROM),
        new MemoryWriteAddress(0x8000, 0xbfff, arabian_videoram_w, videoram),
        new MemoryWriteAddress(0xd000, 0xd7ff, MWA_RAM),
        new MemoryWriteAddress(0xe000, 0xe07f, arabian_blitter_w, spriteram),
        new MemoryWriteAddress(-1) /* end of table */};

    static IOWritePort writeport[] = {
        new IOWritePort(0xc800, 0xc800, AY8910_control_port_0_w),
        new IOWritePort(0xca00, 0xca00, AY8910_write_port_0_w),
        new IOWritePort(-1) /* end of table */};

    static InputPortPtr input_ports_arabian = new InputPortPtr() {
        public void handler() {
            PORT_START();
            /* IN0 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_COIN1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_COIN2);
            PORT_SERVICE(0x04, IP_ACTIVE_HIGH);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* DSW1 */
            PORT_DIPNAME(0x01, 0x00, DEF_STR("Lives"));
            PORT_DIPSETTING(0x00, "3");
            PORT_DIPSETTING(0x01, "5");
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Cabinet"));
            PORT_DIPSETTING(0x02, DEF_STR("Upright"));
            PORT_DIPSETTING(0x00, DEF_STR("Cocktail"));
            PORT_DIPNAME(0x04, 0x04, DEF_STR("Flip_Screen"));
            PORT_DIPSETTING(0x04, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x08, 0x00, "Carry Bowls to Next Life");
            PORT_DIPSETTING(0x08, DEF_STR("No"));
            PORT_DIPSETTING(0x00, DEF_STR("Yes"));
            PORT_DIPNAME(0xf0, 0x00, DEF_STR("Coinage"));
            PORT_DIPSETTING(0x10, "A 2/1 B 2/1");
            PORT_DIPSETTING(0x20, "A 2/1 B 1/3");
            PORT_DIPSETTING(0x00, "A 1/1 B 1/1");
            PORT_DIPSETTING(0x30, "A 1/1 B 1/2");
            PORT_DIPSETTING(0x40, "A 1/1 B 1/3");
            PORT_DIPSETTING(0x50, "A 1/1 B 1/4");
            PORT_DIPSETTING(0x60, "A 1/1 B 1/5");
            PORT_DIPSETTING(0x70, "A 1/1 B 1/6");
            PORT_DIPSETTING(0x80, "A 1/2 B 1/2");
            PORT_DIPSETTING(0x90, "A 1/2 B 1/4");
            PORT_DIPSETTING(0xa0, "A 1/2 B 1/6");
            PORT_DIPSETTING(0xb0, "A 1/2 B 1/10");
            PORT_DIPSETTING(0xc0, "A 1/2 B 1/11");
            PORT_DIPSETTING(0xd0, "A 1/2 B 1/12");
            PORT_DIPSETTING(0xf0, DEF_STR("Free_Play"));
            /* 0xe0 gives A 1/2 B 1/6 */

            PORT_START();
            /* IN1 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_START1);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_START2);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_COIN3);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN2 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN3 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_BUTTON1);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN4 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN | IPF_8WAY | IPF_COCKTAIL);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* IN5 */
            PORT_BIT(0x01, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_COCKTAIL);
            PORT_BIT(0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN);
            PORT_BIT(0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN);

            PORT_START();
            /* DSW2 */
            PORT_DIPNAME(0x01, 0x00, "Coin Chutes");
            PORT_DIPSETTING(0x01, "1");
            PORT_DIPSETTING(0x00, "2");
            PORT_DIPNAME(0x02, 0x02, DEF_STR("Demo_Sounds"));
            PORT_DIPSETTING(0x02, DEF_STR("Off"));
            PORT_DIPSETTING(0x00, DEF_STR("On"));
            PORT_DIPNAME(0x0c, 0x04, DEF_STR("Bonus_Life"));
            PORT_DIPSETTING(0x04, "20000");
            PORT_DIPSETTING(0x08, "40000");
            PORT_DIPSETTING(0x0c, "20000 50000 +100K");
            PORT_DIPSETTING(0x00, "None");
            PORT_DIPNAME(0x10, 0x00, DEF_STR("Unused"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x10, DEF_STR("On"));
            PORT_DIPNAME(0x20, 0x00, DEF_STR("Unused"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x20, DEF_STR("On"));
            PORT_DIPNAME(0x40, 0x00, DEF_STR("Unused"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x40, DEF_STR("On"));
            PORT_DIPNAME(0x80, 0x00, DEF_STR("Unused"));
            PORT_DIPSETTING(0x00, DEF_STR("Off"));
            PORT_DIPSETTING(0x80, DEF_STR("On"));
            INPUT_PORTS_END();
        }
    };

    static AY8910interface ay8910_interface = new AY8910interface(
            1, /* 1 chip */
            1500000, /* 1.5 MHz?????? */
            new int[]{50},
            new ReadHandlerPtr[]{null},
            new ReadHandlerPtr[]{null},
            new WriteHandlerPtr[]{null},
            new WriteHandlerPtr[]{arabian_portB_w});

    static MachineDriver machine_driver_arabian = new MachineDriver(
            /* basic machine hardware */
            new MachineCPU[]{
                new MachineCPU(
                        CPU_Z80 | CPU_16BIT_PORT,
                        4000000, /* 4 MHz */
                        readmem,
                        writemem,
                        null,
                        writeport,
                        arabian_interrupt,
                        1)
            },
            60,
            DEFAULT_60HZ_VBLANK_DURATION, /* frames per second, vblank duration */
            1, /* single CPU, no need for interleaving */
            null,
            /* video hardware */
            256,
            256,
            new rectangle(0, 255, 11, 242),
            null,
            32,
            32,
            arabian_vh_convert_color_prom,
            VIDEO_TYPE_RASTER | VIDEO_SUPPORTS_DIRTY,
            null,
            arabian_vh_start,
            arabian_vh_stop,
            arabian_vh_screenrefresh,
            /* sound hardware */
            0,
            0,
            0,
            0,
            new MachineSound[]{new MachineSound(SOUND_AY8910, ay8910_interface)});

    /**
     * *************************************************************************
     *
     * Game driver(s)
     *
     **************************************************************************
     */
    static RomLoadPtr rom_arabian = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("ic1rev2.87", 0x0000, 0x2000, 0x5e1c98b8);
            ROM_LOAD("ic2rev2.88", 0x2000, 0x2000, 0x092f587e);
            ROM_LOAD("ic3rev2.89", 0x4000, 0x2000, 0x15145f23);
            ROM_LOAD("ic4rev2.90", 0x6000, 0x2000, 0x32b77b44);

            ROM_REGION(0x10000, REGION_GFX1);/* graphics roms */
            ROM_LOAD("ic84.91", 0x0000, 0x2000, 0xc4637822);/* because of very rare way */
            ROM_LOAD("ic85.92", 0x2000, 0x2000, 0xf7c6866d);
            /* CRT controller uses these roms */
            ROM_LOAD("ic86.93", 0x4000, 0x2000, 0x71acd48d);
            /* there's no way, but to decode */
            ROM_LOAD("ic87.94", 0x6000, 0x2000, 0x82160b9a);/* it at runtime - which is SLOW */
            ROM_END();
        }
    };

    static RomLoadPtr rom_arabiana = new RomLoadPtr() {
        public void handler() {
            ROM_REGION(0x10000, REGION_CPU1);/* 64k for code */
            ROM_LOAD("ic1.87", 0x0000, 0x2000, 0x51e9a6b1);
            ROM_LOAD("ic2.88", 0x2000, 0x2000, 0x1cdcc1ab);
            ROM_LOAD("ic3.89", 0x4000, 0x2000, 0xb7b7faa0);
            ROM_LOAD("ic4.90", 0x6000, 0x2000, 0xdbded961);

            ROM_REGION(0x10000, REGION_GFX1);/* graphics roms */
            ROM_LOAD("ic84.91", 0x0000, 0x2000, 0xc4637822);/* because of very rare way */
            ROM_LOAD("ic85.92", 0x2000, 0x2000, 0xf7c6866d);
            /* CRT controller uses these roms */
            ROM_LOAD("ic86.93", 0x4000, 0x2000, 0x71acd48d);
            /* there's no way, but to decode */
            ROM_LOAD("ic87.94", 0x6000, 0x2000, 0x82160b9a);/* it at runtime - which is SLOW */
            ROM_END();
        }
    };

    public static GameDriver driver_arabian = new GameDriver("1983", "arabian", "arabian.java", rom_arabian, null, machine_driver_arabian, input_ports_arabian, null, ROT270, "Sun Electronics", "Arabian", GAME_IMPERFECT_COLORS | GAME_NO_COCKTAIL);
    public static GameDriver driver_arabiana = new GameDriver("1983", "arabiana", "arabian.java", rom_arabiana, driver_arabian, machine_driver_arabian, input_ports_arabian, null, ROT270, "[Sun Electronics] (Atari license)", "Arabian (Atari)", GAME_IMPERFECT_COLORS | GAME_NO_COCKTAIL);
}
