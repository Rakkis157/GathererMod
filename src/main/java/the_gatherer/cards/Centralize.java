package the_gatherer.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import the_gatherer.actions.CentralizeAction;
import the_gatherer.interfaces.OnceEffect;
import the_gatherer.patches.AbstractCardEnum;

public class Centralize extends CustomCard implements OnceEffect {
	public static final String ID = "Centralize";
	private static final CardStrings cardStrings = CardCrawlGame.languagePack.getCardStrings(ID);
	public static final String NAME = cardStrings.NAME;
	public static final String IMG = "img/cards/" + ID + ".png";
	private static final int COST = 1;
	public static final String DESCRIPTION = cardStrings.DESCRIPTION;
	private static final AbstractCard.CardType TYPE = AbstractCard.CardType.SKILL;
	private static final AbstractCard.CardColor COLOR = AbstractCardEnum.LIME;
	private static final AbstractCard.CardRarity RARITY = AbstractCard.CardRarity.BASIC;
	private static final AbstractCard.CardTarget TARGET = AbstractCard.CardTarget.SELF;

	private static final int POWER = 1;
	private static final int UPGRADE_BONUS = 1;

	public Centralize() {
		super(ID, NAME, IMG, COST, DESCRIPTION, TYPE, COLOR, RARITY, TARGET);
		this.baseMagicNumber = POWER;
		this.magicNumber = this.baseMagicNumber;
	}

	public void use(AbstractPlayer p, AbstractMonster m) {
	}

	public AbstractCard makeCopy() {
		return new Centralize();
	}

	public void upgrade() {
		if (!upgraded) {
			upgradeName();
			this.upgradeMagicNumber(UPGRADE_BONUS);
		}
	}

	public void notSingleEffect() {
		AbstractDungeon.actionManager.addToBottom(new CentralizeAction(magicNumber, false));
	}
	public void singleEffect() {
		AbstractDungeon.actionManager.addToBottom(new CentralizeAction(magicNumber, true));
	}
}