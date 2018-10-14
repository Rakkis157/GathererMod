package the_gatherer.potions;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.GameDictionary;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.localization.PotionStrings;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.powers.PoisonPower;
import the_gatherer.patches.PotionRarityEnum;

public class LesserPoisonPotion extends AbstractPotion {
	public static final String POTION_ID = "LesserPoisonPotion";
	private static final PotionStrings potionStrings = CardCrawlGame.languagePack.getPotionString(POTION_ID);
	public static final String NAME = potionStrings.NAME;
	public static final String[] DESCRIPTIONS = potionStrings.DESCRIPTIONS;

	public LesserPoisonPotion() {
		super(NAME, POTION_ID, PotionRarityEnum.LESSER, PotionSize.BOTTLE, PotionColor.POISON);
		this.potency = this.getPotency();
		this.description = DESCRIPTIONS[0] + this.potency + DESCRIPTIONS[1];
		this.isThrown = true;
		this.targetRequired = true;
		this.tips.add(new PowerTip(this.name, this.description));
		this.tips.add(new PowerTip(TipHelper.capitalize(GameDictionary.POISON.NAMES[0]), GameDictionary.keywords.get(GameDictionary.POISON.NAMES[0])));
	}

	public void use(AbstractCreature target) {
		AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(target, AbstractDungeon.player, new PoisonPower(target, AbstractDungeon.player, this.potency), this.potency));
	}

	public AbstractPotion makeCopy() {
		return new LesserPoisonPotion();
	}

	public int getPotency(int ascensionLevel) {
		return 4;
	}
}