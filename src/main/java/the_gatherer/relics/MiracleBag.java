package the_gatherer.relics;

import basemod.abstracts.CustomRelic;
import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import the_gatherer.GathererMod;

import static the_gatherer.GathererMod.logger;

public class MiracleBag extends CustomRelic {
	private static final String RelicID = "MiracleBag";
	public static final String ID = GathererMod.makeID(RelicID);

	public MiracleBag() {
		super(ID, new Texture(GathererMod.GetRelicPath(RelicID)),
				RelicTier.BOSS, LandingSound.FLAT);
		this.counter = 0;
	}

	@Override
	public void obtain() {
		// Code from The-Mystic-Project.
		if (AbstractDungeon.player.hasRelic(AlchemyBag.ID)) {
			this.counter = 1;
			for (int i = 0; i < AbstractDungeon.player.relics.size(); ++i) {
				if (AbstractDungeon.player.relics.get(i).relicId.equals(AlchemyBag.ID)) {
					instantObtain(AbstractDungeon.player, i, true);
					break;
				}
			}
		} else {
			this.counter = 0;
			super.obtain();
		}
	}

	@Override
	public void atBattleStart() {
		AbstractPotion p;
		if (this.counter == 1)
			p = AbstractDungeon.returnRandomPotion(AbstractPotion.PotionRarity.RARE, false);
		else p = AbstractDungeon.returnRandomPotion();
		AbstractDungeon.player.obtainPotion(p);
	}

	@Override
	public String getUpdatedDescription() {
		try {
			if (this.counter == 1 || AbstractDungeon.player.hasRelic(AlchemyBag.ID))
				return DESCRIPTIONS[1];
			else return DESCRIPTIONS[0];
		} catch (NullPointerException npe) {
			logger.info("Miracle Bag: " + npe.getMessage());
			return DESCRIPTIONS[1];
		}
	}

	@Override
	public AbstractRelic makeCopy() {
		return new MiracleBag();
	}
}
