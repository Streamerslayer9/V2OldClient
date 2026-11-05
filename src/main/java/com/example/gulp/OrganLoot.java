package com.example.gulp;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

final class OrganLoot {
   private OrganLoot() {
   }

   static boolean enabled() {
      return (Boolean)GulpServerConfig.ORGAN_LOOT_ENABLED.get();
   }

   private static int amount(CompoundTag entry, float per) {
      return Math.max(1, Math.min(4, Math.round(entry.m_128457_("Size") * per)));
   }

   private static ItemStack milk(String name) {
      return (new ItemStack(Items.f_42455_)).m_41714_(Component.m_237113_(name).m_130938_((st) -> st.m_131155_(false)));
   }

   private static void giveMilk(ServerPlayer p, String name, int count) {
      for(int i = 0; i < Math.min(count, MAX_PER_TAKE); ++i) {
         ItemHandlerHelper.giveItemToPlayer(p, milk(name));
      }

   }

   static boolean mobDigested(ServerPlayer p, Stomach s, CompoundTag entry, int organ) {
      if (!enabled()) {
         return false;
      } else if (organ == Organ.BREAST.ordinal()) {
         addMilk(p, s, entry);
         return true;
      } else if (organ == Organ.COCK.ordinal()) {
         cum(p, entry);
         return true;
      } else if (organ == 3) {
         ItemStack egg = spawnEggOf(entry);
         if (egg == null) {
            return false;
         } else {
            ItemHandlerHelper.giveItemToPlayer(p, egg);
            StomachLogic.say(p, Component.m_237113_("Your womb finished ").m_7220_(StomachLogic.entryName(entry)).m_130946_(": it left a spawn egg."));
            return true;
         }
      } else {
         return false;
      }
   }

   static void playerDigested(ServerPlayer p, Stomach s, CompoundTag entry, int organ) {
      if (enabled()) {
         if (organ == Organ.BREAST.ordinal()) {
            addMilk(p, s, entry);
         } else if (organ == Organ.COCK.ordinal()) {
            cum(p, entry);
         }

      }
   }

   private static void cum(ServerPlayer p, CompoundTag entry) {
      int n = amount(entry, 1.5F);
      giveMilk(p, "Cum", n);
      StomachLogic.say(p, Component.m_237113_("You turned ").m_7220_(StomachLogic.entryName(entry)).m_130946_(" into " + n + " cum."));
   }

   private static void addMilk(ServerPlayer p, Stomach s, CompoundTag entry) {
      int n = amount(entry, 1.5F);
      s.breastMilk += n;
      StomachLogic.say(p, Component.m_237113_("Your breast completely digested ").m_7220_(StomachLogic.entryName(entry)).m_130946_(" into " + n + " Breast Milk."));
   }

   static void tickBreast(ServerPlayer p, StomachManager mgr, Stomach s, boolean hard) {
      if (s.breastMilk > 0 && hard && enabled()) {
         int need = (Integer)GulpServerConfig.BREAST_CHURN_SECONDS.get() * 20;
         if (++s.breastChurn < need) {
            if (s.breastChurn % 20 == 0) {
               StomachLogic.sync(p, s);
            }

         } else {
            s.breastChurn = 0;
            --s.breastMilk;
            ++s.breastButter;
            mgr.m_77762_();
            StomachLogic.sync(p, s);
            StomachLogic.say(p, Component.m_237113_("A batch of Breast Milk was churned into Butter."));
         }
      }
   }

   static final int MAX_PER_TAKE = 32;

   static void take(ServerPlayer p, StomachManager mgr, Stomach s, boolean butter, int requested) {
      int want = Math.max(1, Math.min(MAX_PER_TAKE, requested));
      if (!butter) {
         if (s.breastMilk <= 0) {
            StomachLogic.say(p, Component.m_237113_("There is no Breast Milk to take."));
            return;
         }

         int n = Math.min(want, s.breastMilk);
         s.breastMilk -= n;
         if (s.breastMilk <= 0) {
            s.breastMilk = 0;
            s.breastChurn = 0;
         }

         giveMilk(p, "Breast Milk", n);
         StomachLogic.say(p, Component.m_237113_("You took " + n + " Breast Milk" + (s.breastMilk > 0 ? " (" + s.breastMilk + " left)." : ".")));
      } else {
         if (s.breastButter <= 0) {
            StomachLogic.say(p, Component.m_237113_("There is no Butter ready yet."));
            return;
         }

         int n = Math.min(want, s.breastButter);
         s.breastButter -= n;
         if (s.breastButter < 0) {
            s.breastButter = 0;
         }

         for(int left = n; left > 0; left -= 64) {
            ItemHandlerHelper.giveItemToPlayer(p, new ItemStack((ItemLike)Gulp.BUTTER.get(), Math.min(64, left)));
         }

         StomachLogic.say(p, Component.m_237113_("You took " + n + " Butter" + (s.breastButter > 0 ? " (" + s.breastButter + " left)." : ".")).m_130940_(ChatFormatting.YELLOW));
      }

      mgr.m_77762_();
      StomachLogic.sync(p, s);
   }

   static ItemStack spawnEggOf(CompoundTag entry) {
      ResourceLocation id = ResourceLocation.m_135820_(entry.m_128469_("Entity").m_128461_("id"));
      if (id == null) {
         return null;
      } else {
         EntityType<?> type = (EntityType)ForgeRegistries.ENTITY_TYPES.getValue(id);
         if (type == null) {
            return null;
         } else {
            SpawnEggItem egg = SpawnEggItem.m_43213_(type);
            return egg == null ? null : new ItemStack(egg);
         }
      }
   }
}
