package WayofTime.alchemicalWizardry.common.rituals;

import java.util.ArrayList;
import java.util.List;

import WayofTime.alchemicalWizardry.AlchemicalWizardry;
import WayofTime.alchemicalWizardry.api.alchemy.energy.ReagentRegistry;
import WayofTime.alchemicalWizardry.api.rituals.IMasterRitualStone;
import WayofTime.alchemicalWizardry.api.rituals.RitualComponent;
import WayofTime.alchemicalWizardry.api.rituals.RitualEffect;
import WayofTime.alchemicalWizardry.api.soulNetwork.SoulNetworkHandler;
import WayofTime.alchemicalWizardry.api.tile.IBloodAltar;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class RitualEffectBloodSiphon extends RitualEffect {
	public static final int timeDelay = 25;
	public static final int amount = AlchemicalWizardry.lpPerSacrificeBloodSiphon;

	private static final int tennebraeDrain = 5;
	private static final int potentiaDrain = 10;
	private static final int offensaDrain = 3;

	@Override
	public void performEffect(IMasterRitualStone ritualStone) {
		String owner = ritualStone.getOwner();
		int currentEssence = SoulNetworkHandler.getCurrentEssence(owner);

		World world = ritualStone.getWorld();
		int x = ritualStone.getXCoord();
		int y = ritualStone.getYCoord();
		int z = ritualStone.getZCoord();

		if (world.getWorldTime() % this.timeDelay != 0) {
			return;
		}

		IBloodAltar tileAltar = null;
		boolean testFlag = false;

		for (int i = -5; i <= 5; i++) {
			for (int j = -5; j <= 5; j++) {
				for (int k = -13; k <= 13; k++)// height checking is +6 because it's height is higher than normal
				{
					if (world.getTileEntity(x + i, y + k, z + j) instanceof IBloodAltar) {
						tileAltar = (IBloodAltar) world.getTileEntity(x + i, y + k, z + j);
						testFlag = true;
					}
				}
			}
		}

		if (!testFlag) {
			return;
		}

		boolean hasPotentia = this.canDrainReagent(ritualStone, ReagentRegistry.potentiaReagent, potentiaDrain, false);

		int d0 = 10;
		int vertRange = hasPotentia ? 20 : 16;
		AxisAlignedBB axisalignedbb = AxisAlignedBB.getBoundingBox((double) x, (double) y, (double) z, (double) (x + 1), (double) (y + 1), (double) (z + 1)).expand(d0, vertRange, d0);
		List<EntityLivingBase> list = world.getEntitiesWithinAABB(EntityLivingBase.class, axisalignedbb);

		int entityCount = 0;
		boolean hasTennebrae = this.canDrainReagent(ritualStone, ReagentRegistry.tenebraeReagent, tennebraeDrain, false);
		boolean hasOffensa = this.canDrainReagent(ritualStone, ReagentRegistry.offensaReagent, offensaDrain, false);

		if (currentEssence < this.getCostPerRefresh() * list.size()) {
			SoulNetworkHandler.causeNauseaToPlayer(owner);
		} else {
			for (EntityLivingBase livingEntity : list) {
				if (!livingEntity.isEntityAlive() || livingEntity instanceof EntityPlayer || AlchemicalWizardry.wellBlacklist.contains(livingEntity.getClass())) {
					continue;
				}

				hasOffensa = hasOffensa && this.canDrainReagent(ritualStone, ReagentRegistry.offensaReagent, offensaDrain, true);

				if (livingEntity.attackEntityFrom(DamageSource.outOfWorld, hasOffensa ? 2 : 1)) {
					hasTennebrae = hasTennebrae && this.canDrainReagent(ritualStone, ReagentRegistry.tenebraeReagent, tennebraeDrain, true);
					entityCount++;
					if (entityCount <= AlchemicalWizardry.maxEntitiesCounted) {
						tileAltar.sacrificialDaggerCall(this.amount * (hasTennebrae ? 2 : 1) * (hasOffensa ? 2 : 1), true);
					}
					else {// prevent more than <config entry> entities from being counted
						tileAltar.sacrificialDaggerCall(AlchemicalWizardry.maxEntitiesCounted * (hasTennebrae ? 2 : 1) * (hasOffensa ? 2 : 1), true);
					}
				}
			}

			if (entityCount <= AlchemicalWizardry.maxEntitiesCounted) {
				SoulNetworkHandler.syphonFromNetwork(owner, this.getCostPerRefresh() * entityCount);
			} else if (entityCount > AlchemicalWizardry.maxEntitiesCounted) {// if more than <config entry> entities, only pay for <config entry> to be injured
				SoulNetworkHandler.syphonFromNetwork(owner, this.getCostPerRefresh() * 20);
			}

			if (hasPotentia) {
				this.canDrainReagent(ritualStone, ReagentRegistry.potentiaReagent, potentiaDrain, true);
			}
		}
	}

	@Override
	public int getCostPerRefresh() {
		return AlchemicalWizardry.ritualCostBloodSiphon[1];
	}

	@Override
	public List<RitualComponent> getRitualComponentList() {
		ArrayList<RitualComponent> bloodSiphonRitual = new ArrayList();
		// fire
		bloodSiphonRitual.add(new RitualComponent(-1, 0, 0, RitualComponent.FIRE));

		bloodSiphonRitual.add(new RitualComponent(-2, 1, 1, RitualComponent.FIRE));
		bloodSiphonRitual.add(new RitualComponent(-2, 1, -1, RitualComponent.FIRE));
		bloodSiphonRitual.add(new RitualComponent(-2, -1, 1, RitualComponent.FIRE));
		bloodSiphonRitual.add(new RitualComponent(-2, -1, -1, RitualComponent.FIRE));

		bloodSiphonRitual.add(new RitualComponent(-3, 2, 0, RitualComponent.FIRE));
		bloodSiphonRitual.add(new RitualComponent(-3, -2, 0, RitualComponent.FIRE));

		bloodSiphonRitual.add(new RitualComponent(-4, 3, 1, RitualComponent.FIRE));
		bloodSiphonRitual.add(new RitualComponent(-4, 4, 0, RitualComponent.FIRE));
		bloodSiphonRitual.add(new RitualComponent(-4, 3, -1, RitualComponent.FIRE));

		bloodSiphonRitual.add(new RitualComponent(-4, -3, 1, RitualComponent.FIRE));
		bloodSiphonRitual.add(new RitualComponent(-4, -4, 0, RitualComponent.FIRE));
		bloodSiphonRitual.add(new RitualComponent(-4, -3, -1, RitualComponent.FIRE));

		// water
		bloodSiphonRitual.add(new RitualComponent(1, 0, 0, RitualComponent.WATER));

		bloodSiphonRitual.add(new RitualComponent(2, 1, 1, RitualComponent.WATER));
		bloodSiphonRitual.add(new RitualComponent(2, 1, -1, RitualComponent.WATER));
		bloodSiphonRitual.add(new RitualComponent(2, -1, 1, RitualComponent.WATER));
		bloodSiphonRitual.add(new RitualComponent(2, -1, -1, RitualComponent.WATER));

		bloodSiphonRitual.add(new RitualComponent(3, 2, 0, RitualComponent.WATER));
		bloodSiphonRitual.add(new RitualComponent(3, -2, 0, RitualComponent.WATER));

		bloodSiphonRitual.add(new RitualComponent(4, 3, 1, RitualComponent.WATER));
		bloodSiphonRitual.add(new RitualComponent(4, 4, 0, RitualComponent.WATER));
		bloodSiphonRitual.add(new RitualComponent(4, 3, -1, RitualComponent.WATER));

		bloodSiphonRitual.add(new RitualComponent(4, -3, 1, RitualComponent.WATER));
		bloodSiphonRitual.add(new RitualComponent(4, -4, 0, RitualComponent.WATER));
		bloodSiphonRitual.add(new RitualComponent(4, -3, -1, RitualComponent.WATER));

		// Air
		bloodSiphonRitual.add(new RitualComponent(0, 0, -1, RitualComponent.AIR));

		bloodSiphonRitual.add(new RitualComponent(1, 1, -2, RitualComponent.AIR));
		bloodSiphonRitual.add(new RitualComponent(-1, 1, -2, RitualComponent.AIR));
		bloodSiphonRitual.add(new RitualComponent(1, -1, -2, RitualComponent.AIR));
		bloodSiphonRitual.add(new RitualComponent(-1, -1, -2, RitualComponent.AIR));

		bloodSiphonRitual.add(new RitualComponent(0, 2, -3, RitualComponent.AIR));
		bloodSiphonRitual.add(new RitualComponent(0, -2, -3, RitualComponent.AIR));

		bloodSiphonRitual.add(new RitualComponent(1, 3, -4, RitualComponent.AIR));
		bloodSiphonRitual.add(new RitualComponent(0, 4, -4, RitualComponent.AIR));
		bloodSiphonRitual.add(new RitualComponent(-1, 3, -4, RitualComponent.AIR));

		bloodSiphonRitual.add(new RitualComponent(1, -3, -4, RitualComponent.AIR));
		bloodSiphonRitual.add(new RitualComponent(0, -4, -4, RitualComponent.AIR));
		bloodSiphonRitual.add(new RitualComponent(-1, -3, -4, RitualComponent.AIR));

		// earth
		bloodSiphonRitual.add(new RitualComponent(0, 0, 1, RitualComponent.EARTH));

		bloodSiphonRitual.add(new RitualComponent(1, 1, 2, RitualComponent.EARTH));
		bloodSiphonRitual.add(new RitualComponent(-1, 1, 2, RitualComponent.EARTH));
		bloodSiphonRitual.add(new RitualComponent(1, -1, 2, RitualComponent.EARTH));
		bloodSiphonRitual.add(new RitualComponent(-1, -1, 2, RitualComponent.EARTH));

		bloodSiphonRitual.add(new RitualComponent(0, 2, 3, RitualComponent.EARTH));
		bloodSiphonRitual.add(new RitualComponent(0, -2, 3, RitualComponent.EARTH));

		bloodSiphonRitual.add(new RitualComponent(1, 3, 4, RitualComponent.EARTH));
		bloodSiphonRitual.add(new RitualComponent(0, 4, 4, RitualComponent.EARTH));
		bloodSiphonRitual.add(new RitualComponent(-1, 3, 4, RitualComponent.EARTH));

		bloodSiphonRitual.add(new RitualComponent(1, -3, 4, RitualComponent.EARTH));
		bloodSiphonRitual.add(new RitualComponent(0, -4, 4, RitualComponent.EARTH));
		bloodSiphonRitual.add(new RitualComponent(-1, -3, 4, RitualComponent.EARTH));

		// dusk
		bloodSiphonRitual.add(new RitualComponent(3, -2, -2, RitualComponent.DUSK));
		bloodSiphonRitual.add(new RitualComponent(2, -2, -3, RitualComponent.DUSK));

		bloodSiphonRitual.add(new RitualComponent(4, -3, -3, RitualComponent.DUSK));
		bloodSiphonRitual.add(new RitualComponent(3, -3, -4, RitualComponent.DUSK));

		bloodSiphonRitual.add(new RitualComponent(4, -4, -2, RitualComponent.DUSK));
		bloodSiphonRitual.add(new RitualComponent(2, -4, -4, RitualComponent.DUSK));
		bloodSiphonRitual.add(new RitualComponent(4, -4, -4, RitualComponent.DUSK));

		bloodSiphonRitual.add(new RitualComponent(-3, -2, -2, RitualComponent.DUSK));
		bloodSiphonRitual.add(new RitualComponent(-2, -2, -3, RitualComponent.DUSK));

		bloodSiphonRitual.add(new RitualComponent(-4, -3, -3, RitualComponent.DUSK));
		bloodSiphonRitual.add(new RitualComponent(-3, -3, -4, RitualComponent.DUSK));

		bloodSiphonRitual.add(new RitualComponent(-4, -4, -2, RitualComponent.DUSK));
		bloodSiphonRitual.add(new RitualComponent(-2, -4, -4, RitualComponent.DUSK));
		bloodSiphonRitual.add(new RitualComponent(-4, -4, -4, RitualComponent.DUSK));

		bloodSiphonRitual.add(new RitualComponent(-3, -2, 2, RitualComponent.DUSK));
		bloodSiphonRitual.add(new RitualComponent(-2, -2, 3, RitualComponent.DUSK));

		bloodSiphonRitual.add(new RitualComponent(-4, -3, 3, RitualComponent.DUSK));
		bloodSiphonRitual.add(new RitualComponent(-3, -3, 4, RitualComponent.DUSK));

		bloodSiphonRitual.add(new RitualComponent(-4, -4, 2, RitualComponent.DUSK));
		bloodSiphonRitual.add(new RitualComponent(-2, -4, 4, RitualComponent.DUSK));
		bloodSiphonRitual.add(new RitualComponent(-4, -4, 4, RitualComponent.DUSK));

		bloodSiphonRitual.add(new RitualComponent(3, -2, 2, RitualComponent.DUSK));
		bloodSiphonRitual.add(new RitualComponent(2, -2, 3, RitualComponent.DUSK));

		bloodSiphonRitual.add(new RitualComponent(4, -3, 3, RitualComponent.DUSK));
		bloodSiphonRitual.add(new RitualComponent(3, -3, 4, RitualComponent.DUSK));

		bloodSiphonRitual.add(new RitualComponent(4, -4, 2, RitualComponent.DUSK));
		bloodSiphonRitual.add(new RitualComponent(2, -4, 4, RitualComponent.DUSK));
		bloodSiphonRitual.add(new RitualComponent(4, -4, 4, RitualComponent.DUSK));

		// dawn
		bloodSiphonRitual.add(new RitualComponent(3, 2, -2, RitualComponent.DAWN));
		bloodSiphonRitual.add(new RitualComponent(2, 2, -3, RitualComponent.DAWN));

		bloodSiphonRitual.add(new RitualComponent(4, 3, -3, RitualComponent.DAWN));
		bloodSiphonRitual.add(new RitualComponent(3, 3, -4, RitualComponent.DAWN));

		bloodSiphonRitual.add(new RitualComponent(4, 4, -2, RitualComponent.DAWN));
		bloodSiphonRitual.add(new RitualComponent(2, 4, -4, RitualComponent.DAWN));
		bloodSiphonRitual.add(new RitualComponent(4, 4, -4, RitualComponent.DAWN));

		bloodSiphonRitual.add(new RitualComponent(-3, 2, -2, RitualComponent.DAWN));
		bloodSiphonRitual.add(new RitualComponent(-2, 2, -3, RitualComponent.DAWN));

		bloodSiphonRitual.add(new RitualComponent(-4, 3, -3, RitualComponent.DAWN));
		bloodSiphonRitual.add(new RitualComponent(-3, 3, -4, RitualComponent.DAWN));

		bloodSiphonRitual.add(new RitualComponent(-4, 4, -2, RitualComponent.DAWN));
		bloodSiphonRitual.add(new RitualComponent(-2, 4, -4, RitualComponent.DAWN));
		bloodSiphonRitual.add(new RitualComponent(-4, 4, -4, RitualComponent.DAWN));

		bloodSiphonRitual.add(new RitualComponent(-3, 2, 2, RitualComponent.DAWN));
		bloodSiphonRitual.add(new RitualComponent(-2, 2, 3, RitualComponent.DAWN));

		bloodSiphonRitual.add(new RitualComponent(-4, 3, 3, RitualComponent.DAWN));
		bloodSiphonRitual.add(new RitualComponent(-3, 3, 4, RitualComponent.DAWN));

		bloodSiphonRitual.add(new RitualComponent(-4, 4, 2, RitualComponent.DAWN));
		bloodSiphonRitual.add(new RitualComponent(-2, 4, 4, RitualComponent.DAWN));
		bloodSiphonRitual.add(new RitualComponent(-4, 4, 4, RitualComponent.DAWN));

		bloodSiphonRitual.add(new RitualComponent(3, 2, 2, RitualComponent.DAWN));
		bloodSiphonRitual.add(new RitualComponent(2, 2, 3, RitualComponent.DAWN));

		bloodSiphonRitual.add(new RitualComponent(4, 3, 3, RitualComponent.DAWN));
		bloodSiphonRitual.add(new RitualComponent(3, 3, 4, RitualComponent.DAWN));

		bloodSiphonRitual.add(new RitualComponent(4, 4, 2, RitualComponent.DAWN));
		bloodSiphonRitual.add(new RitualComponent(2, 4, 4, RitualComponent.DAWN));
		bloodSiphonRitual.add(new RitualComponent(4, 4, 4, RitualComponent.DAWN));
		return bloodSiphonRitual;
	}

}
