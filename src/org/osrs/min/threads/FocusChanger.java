package org.osrs.min.threads;

import org.osrs.min.api.data.Game;
import org.osrs.min.api.packets.outgoing.PacketWriter;
import org.parabot.api.calculations.Random;
import org.parabot.environment.api.utils.Time;

/**
 * In here we constantly change the focus of the client (in the background, so you won't notice anything).
 * <p>
 * This is an attempt to throw off Jagex's bot detection system.
 * <p>
 * A packet gets sent to the OSRS server every time focus is gained/lost. (rev.185 - ClientPacket (10, 1)).
 * <p>
 * We're basically just sending the packet at random times, to simulate focus gain/loss.
 */

public class FocusChanger extends Thread {

    public void run() {
        while (true) {

            Time.sleep(Random.between(1500, 2000));
            if (!Game.isLoggedIn())
                continue;
            if (PacketWriter.getInstance() == null)
                continue;
            if (!needsRandomFocusChange())
                continue;

            PacketWriter.getInstance().sendRandomFocus();
            PacketWriter.getInstance().sendRandomDownsize();
        }
    }

    private final boolean needsRandomFocusChange() {
        if ((System.currentTimeMillis() & 1 << 18) > 0) {
            return Random.between(1, 10) == 1;
        }
        return Random.between(1, 4) == 1;
    }

}
//ItemDef error...
//Error: hz.a() bx.al() bx.n() | | java.lang.NullPointerException | 278 509 277 50


/*
Error: hz.a() hs.ay() bx.n() | | java.lang.NullPointerException | 2618 2611 2610
        6515
        Error: hz.a() bx.al() bx.n() | | java.lang.NullPointerException | 278 509 277 50
        3 274 4209
        Error: hz.a() bx.al() bx.n() | | java.lang.NullPointerException | 1272 1270 4209

        Error: hz.a() ff.c() bx.n() | | java.lang.NullPointerException | 297 296 294 120

        Error: hz.a() bj.a() gf.o() n.c() li.t() | | java.lang.NullPointerException | 41
        ,28,7,97,3167,3488,33,-107,77,-128,24,108,121,122,24,-96,-38,-39,118,35,46,0,4,1
        ,107,-128,6,4,29,107,-128,-78,1,0,0,0,0,106,26,-126,-128,7,-54,0,0,-1,21,64,43,3
        ,8,-82,37,-31,11,-57,*/
