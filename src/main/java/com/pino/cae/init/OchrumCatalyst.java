package com.pino.cae.init;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;

import static net.minecraft.world.item.Items.REDSTONE;

@Mod.EventBusSubscriber

public class OchrumCatalyst extends Block {

    private static final Logger LOGGER = LogUtils.getLogger(); // remember son: if you feel like you're gonna have problems, this line gotta be first one
    public OchrumCatalyst(Properties p_49795_) {
        super(p_49795_);
    }
    public static final Random RAND = new Random();
    public static final int TICKS = 120; // time it takes at minimum




    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
          // good practice to have random static and final :D
         int RADIUS = RAND.nextInt(4) + 2; // using finals is considered a good practice.
         int CHANCE = 60 / RADIUS; // chance in %, in case it's too dense / densen't

        if (!world.isClientSide() && held.getItem() == REDSTONE.asItem()){
            if (!player.getAbilities().instabuild) held.shrink(1); // first shrink, becasue we don't want to delay on that

            for (int x = -RADIUS; x <= RADIUS; x++) {
                for (int y = -RADIUS; y <= RADIUS; y++) {
                    for (int z = -RADIUS; z <= RADIUS; z++) {
                        var temp = pos.offset(x, y, z);
                        int random = RAND.nextInt(100);
                        // BlockState block = world.getBlockState(pos); - redunant line, we already have "BlockState state"
                        if (x == 0 && y == 0 && z == 0 || !world.getBlockState(temp).toString().contains("minecraft:air")) continue;// not sure what did this line do, so I replaced it with central block check.
                        // if (!world.getBlockState(temp).is(Blocks.AIR)) continue; // uncomment if you only want to convert AIR blocks.
                        if (random <= CHANCE) // 23 works, 71 dosn't (if CHANCE = 50)
                        {
                            //LOGGER.debug("LES GO ITS ACTUALLY KINDA WORKING probably");
                            schedulePlacement(temp, world);
                            //world.setBlockAndUpdate(temp, Blocks.COBBLESTONE.defaultBlockState()); //insert 10 tick delay here
                        }
                    }
                }
            }

            return InteractionResult.SUCCESS; // looks better than CONSUME no cap (might change back if you wanna)
        }
        return super.use(state, world, pos, player, hand, hit);
    }
    public static final Hashtable<BlockPos, Tuple<Integer, LevelAccessor>> schedule = new Hashtable<>(); // java won't let use primitives >:(
    public static void schedulePlacement(BlockPos bp, LevelAccessor la) {
        if (schedule.contains(bp)) return;

        schedule.put(bp, new Tuple<>(RAND.nextInt(TICKS, TICKS * 2), la));
        //schedule.put(bp, TICKS); // uncomment me and comment hting above if you want no random;
        //LOGGER.debug("omfg I exist now!!" + schedule.get(bp).toString());
    }
    @SubscribeEvent
    public static void tickEvent(TickEvent.WorldTickEvent w) {
        ArrayList<BlockPos> removeMe = new ArrayList<>();
        schedule.forEach(((blockPos, tuple) -> {
            if (!w.world.equals(tuple.getB())) {
                //LOGGER.warn("WORLD DIFF!!");
                return;
            }
            if (tuple.getA() <= 0) {
                //LOGGER.debug("good bye");
                removeMe.add(blockPos);
                tuple.getB().setBlock(blockPos, GeodeShit.Ochrum.defaultBlockState(), 3);
                return;
            }
            tuple.setA(tuple.getA() - 1);
        }));
        removeMe.forEach(schedule::remove);
        removeMe.clear(); // just in case
    }
}
//code by the beautifull omga