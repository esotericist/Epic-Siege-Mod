package funwayguy.epicsiegemod.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class ESM_EntityAIPillarUp extends EntityAIBase
{
	/**
	 * Potential surfaces zombies can initialise pillaring on
	 */
	static final EnumFacing[] placeSurface = new EnumFacing[]{EnumFacing.DOWN,EnumFacing.NORTH,EnumFacing.EAST,EnumFacing.SOUTH,EnumFacing.WEST};
	public int placeDelay = 15;
	public EntityLiving builder;
	public EntityLivingBase target;
	
	BlockPos blockPos;
	
	public ESM_EntityAIPillarUp(EntityLiving entity)
	{
		this.builder = entity;
	}

	@Override
	public boolean shouldExecute()
	{
		target = builder.getAttackTarget();
		
		if(target == null || !target.isEntityAlive())
		{
			return false;
		}
		
		if(builder.getNavigator().noPath() && ((builder.getDistance(target.posX, builder.posY, target.posZ) < 4D && builder.onGround) || builder.isInLava() || builder.isInWater()))
		{
			BlockPos tmpPos = builder.getPosition();
			BlockPos orgPos = tmpPos;
			
			int xOff = (int)Math.signum(MathHelper.floor_double(target.posX) - orgPos.getX());
			int zOff = (int)Math.signum(MathHelper.floor_double(target.posZ) - orgPos.getZ());
			
			boolean canPlace = false;
			
			for(EnumFacing dir : placeSurface)
			{
				if(builder.worldObj.getBlockState(tmpPos.offset(dir)).isNormalCube())
				{
					canPlace = true;
					break;
				}
			}
			
			if(target.posY - builder.posY < 16 && builder.worldObj.getBlockState(tmpPos.add(0, -2, 0)).isNormalCube() && builder.worldObj.getBlockState(tmpPos.add(0, -1, 0)).isNormalCube()) // Sideways pillaring
			{
				if(builder.worldObj.getBlockState(tmpPos.add(xOff, -1, 0)).getMaterial().isReplaceable())
				{
					tmpPos = tmpPos.add(xOff, -1, 0);
				} else if(builder.worldObj.getBlockState(tmpPos.add(0, -1, zOff)).getMaterial().isReplaceable())
				{
					tmpPos = tmpPos.add(0, -1, zOff);
				} else if(target.posY <= builder.posY)
				{
					return false;
				}
			} else if(target.posY <= builder.posY)
			{
				return false;
			}
			
			if(!canPlace || builder.worldObj.getBlockState(orgPos.add(0, 2, 0)).getMaterial().blocksMovement() || builder.worldObj.getBlockState(tmpPos.add(0, 2, 0)).getMaterial().blocksMovement())
			{
				return false;
			}
			
			blockPos = tmpPos;
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public void startExecuting()
	{
		placeDelay = 15;
	}
	
	@Override
	public boolean continueExecuting()
	{
		return shouldExecute();
	}
	
	@Override
	public void updateTask()
	{
		if(placeDelay > 0 || target == null)
		{
			placeDelay--;
			return;
		} else if(blockPos != null)
		{
			placeDelay = 15;

			builder.setPositionAndUpdate(blockPos.getX() + 0.5D, blockPos.getY() + 1D, blockPos.getZ() + 0.5D);
			
			if(builder.worldObj.getBlockState(blockPos).getMaterial().isReplaceable())
			{
				builder.worldObj.setBlockState(blockPos, Blocks.COBBLESTONE.getDefaultState());
			}
			
			builder.getNavigator().setPath(builder.getNavigator().getPathToEntityLiving(target), builder.getMoveHelper().getSpeed());
		}
	}

    @Override
    public boolean isInterruptible()
    {
        return false;
    }
}
