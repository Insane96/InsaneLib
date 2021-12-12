package insane96mcp.insanelib.entity;

import com.google.common.collect.Lists;
import insane96mcp.insanelib.setup.ModEntities;
import insane96mcp.insanelib.utils.RandomHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

public class AreaEffectCloud3DEntity extends AreaEffectCloud {
	public AreaEffectCloud3DEntity(EntityType<? extends AreaEffectCloud3DEntity> cloud, Level world) {
		super(cloud, world);
	}

	public AreaEffectCloud3DEntity(Level worldIn, double x, double y, double z) {
		this(ModEntities.AREA_EFFECT_CLOUD_3D.get(), worldIn);
		this.setPos(x, y, z);
	}

	public AreaEffectCloud3DEntity(AreaEffectCloud areaEffectCloudEntity) {
		this(ModEntities.AREA_EFFECT_CLOUD_3D.get(), areaEffectCloudEntity.level);
		this.setPos(areaEffectCloudEntity.getX(), areaEffectCloudEntity.getY(), areaEffectCloudEntity.getZ());
		CompoundTag nbt = new CompoundTag();
		areaEffectCloudEntity.saveAsPassenger(nbt);
		this.readAdditionalSaveData(nbt);
	}

	@Override
	public void refreshDimensions() {
		super.refreshDimensions();
		double radius = (double)this.getDimensions(Pose.STANDING).width / 2.0D;
		this.setBoundingBox(new AABB(this.getX() - radius, this.getY() - radius, this.getZ() - radius, this.getX() + radius, this.getY() + radius, this.getZ() + radius));
	}

	@Override
	public void tick() {
		boolean flag = this.isWaiting();
		float f = this.getRadius();
		if (this.level.isClientSide) {
			ParticleOptions iparticledata = this.getParticle();
			if (flag) {
				if (this.random.nextBoolean()) {
					for(int i = 0; i < 2; ++i) {
						float f1 = this.random.nextFloat() * ((float)Math.PI * 2F);
						float f2 = Mth.sqrt(this.random.nextFloat()) * 0.2F;
						float x = Mth.cos(f1) * f2;
						float z = Mth.sin(f1) * f2;
						if (iparticledata.getType() == ParticleTypes.ENTITY_EFFECT) {
							int j = this.random.nextBoolean() ? 16777215 : this.getColor();
							int k = j >> 16 & 255;
							int l = j >> 8 & 255;
							int i1 = j & 255;
							this.level.addParticle(iparticledata, this.getX() + (double)x, this.getY(), this.getZ() + (double)z, (double)((float)k / 255.0F), (double)((float)l / 255.0F), (double)((float)i1 / 255.0F));
						} else {
							this.level.addParticle(iparticledata, this.getX() + (double)x, this.getY(), this.getZ() + (double)z, 0.0D, 0.0D, 0.0D);
						}
					}
				}
			} else {
				float f5 = (float)Math.PI * f * f * 2;

				for(int k1 = 0; (float)k1 < f5; ++k1) {
					float f6 = this.random.nextFloat() * ((float)Math.PI * 2F);
					float f7 = Mth.sqrt(this.random.nextFloat()) * f;
					float x = RandomHelper.getFloat(this.random, -f, f);
					float y = RandomHelper.getFloat(this.random, -f, f);
					float z = RandomHelper.getFloat(this.random, -f, f);
					if ((x*x) + (y*y) + (z*z) > (f*f))
						continue;

					if (iparticledata.getType() == ParticleTypes.ENTITY_EFFECT) {
						int l1 = this.getColor();
						int i2 = l1 >> 16 & 255;
						int j2 = l1 >> 8 & 255;
						int j1 = l1 & 255;
						this.level.addParticle(iparticledata, this.getX() + (double)x, this.getY() + (double)y, this.getZ() + (double)z, (double)((float)i2 / 255.0F), (double)((float)j2 / 255.0F), (double)((float)j1 / 255.0F));
					} else {
						this.level.addParticle(iparticledata, this.getX() + (double)x, this.getY() + (double)y, this.getZ() + (double)z, (0.5D - this.random.nextDouble()) * 0.15D, (double)0.01F, (0.5D - this.random.nextDouble()) * 0.15D);
					}
				}
			}
		} else {
			if (this.tickCount >= this.waitTime + this.duration) {
				this.discard();
				return;
			}

			boolean flag1 = this.tickCount < this.waitTime;
			if (flag != flag1) {
				this.setWaiting(flag1);
			}

			if (flag1) {
				return;
			}

			if (this.radiusPerTick != 0.0F) {
				f += this.radiusPerTick;
				if (f < 0.5F) {
					this.discard();
					return;
				}

				this.setRadius(f);
			}

			if (this.tickCount % 5 == 0) {
				this.victims.entrySet().removeIf(entry -> this.tickCount >= entry.getValue());

				List<MobEffectInstance> list = Lists.newArrayList();

				for(MobEffectInstance effectinstance1 : this.potion.getEffects()) {
					list.add(new MobEffectInstance(effectinstance1.getEffect(), effectinstance1.getDuration() / 4, effectinstance1.getAmplifier(), effectinstance1.isAmbient(), effectinstance1.isVisible()));
				}

				list.addAll(this.effects);
				if (list.isEmpty()) {
					this.victims.clear();
				} else {
					List<LivingEntity> list1 = this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox());
					if (!list1.isEmpty()) {
						for(LivingEntity livingentity : list1) {
							if (!this.victims.containsKey(livingentity) && livingentity.isAffectedByPotions()) {
								this.victims.put(livingentity, this.tickCount + this.reapplicationDelay);
								double x = livingentity.getX() - this.getX();
								double y = livingentity.getY() + (livingentity.getDimensions(livingentity.getPose()).height / 2) - (this.getY());
								double z = livingentity.getZ() - this.getZ();
								double d2 = x * x + y * y + z * z;
								if (d2 <= (double)(f * f)) {
									for (MobEffectInstance effectinstance : list) {
										if (effectinstance.getEffect().isInstantenous()) {
											effectinstance.getEffect().applyInstantenousEffect(this, this.getOwner(), livingentity, effectinstance.getAmplifier(), 0.5D);
										}
										else {
											livingentity.addEffect(new MobEffectInstance(effectinstance));
										}
									}
									if (this.radiusOnUse != 0.0F) {
										f += this.radiusOnUse;
										if (f < 0.5F) {
											this.discard();
											return;
										}
										this.setRadius(f);
									}
									if (this.durationOnUse != 0) {
										this.duration += this.durationOnUse;
										if (this.duration <= 0) {
											this.discard();
											return;
										}
									}
								}
							}
						}
					}
				}
			}
		}

	}

	@Override
	public EntityDimensions getDimensions(Pose poseIn) {
		return EntityDimensions.scalable(this.getRadius() * 2.0F, this.getRadius() * 2.0F);
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}
