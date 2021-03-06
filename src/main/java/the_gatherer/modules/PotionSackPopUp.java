package the_gatherer.modules;

import basemod.BaseMod;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.GameCursor;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.shrines.WeMeetAgain;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.helpers.controller.CInputActionSet;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.potions.FruitJuice;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.ui.panels.PotionPopUp;
import the_gatherer.GathererMod;

import java.util.ArrayList;
import java.util.Iterator;

public class PotionSackPopUp {
	private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString("PotionPopUp");
	public static final String[] TEXT = uiStrings.TEXT;
	private static final String THROW_LABEL = TEXT[0];
	private static final String DRINK_LABEL = TEXT[1];
	private static final String DISCARD_LABEL = TEXT[2];
	private int slot;
	private AbstractPotion potion;
	public boolean isHidden = true;
	public boolean targetMode = false;

	private static final int RAW_W = 282;
	private static final int RAW_H = 286;
	private static final float HB_W = 184.0F;
	private static final float HB_H = 52.0F;
	private Hitbox hbTop = new Hitbox(HB_W, HB_H);
	private Hitbox hbBot = new Hitbox(HB_W, HB_H);
	private float x;
	private float y;
	private static final int SEGMENTS = 20;
	private Vector2[] points = new Vector2[SEGMENTS];
	private Vector2 controlPoint;
	private float arrowScale;
	private float arrowScaleTimer = 0.0F;
	private static final float ARROW_TARGET_SCALE = 1.2F;
	private static final int TARGET_ARROW_W = 256;
	private AbstractMonster hoveredMonster = null;
	private boolean autoTargetFirst = false;

	public PotionSackPopUp() {
		for (int i = 0; i < this.points.length; ++i) {
			this.points[i] = new Vector2();
		}

	}

	public void open(int slot, AbstractPotion potion) {
		AbstractDungeon.topPanel.selectPotionMode = false;
		this.slot = slot;
		this.potion = potion;
		this.x = potion.posX;
		this.y = potion.posY + 160.0F * Settings.scale;
		this.isHidden = false;
		this.hbBot.move(this.x, this.y - 44.0F * Settings.scale);
		this.hbTop.move(this.x, this.y + 14.0F * Settings.scale);
		this.hbBot.clickStarted = false;
		this.hbTop.clickStarted = false;
		this.hbBot.clicked = false;
		this.hbTop.clicked = false;
	}

	public void close() {
		this.isHidden = true;
	}

	public void update() {
		if (!this.isHidden) {
			this.updateControllerInput();
			this.hbBot.update();
			this.hbTop.update();
			this.updateInput();
		} else if (this.targetMode) {
			this.updateControllerTargetInput();
			this.updateTargetMode();
		}

	}

	private void updateControllerTargetInput() {
		if (Settings.isControllerMode) {
			int offsetEnemyIndex = 0;
			if (this.autoTargetFirst) {
				this.autoTargetFirst = false;
				++offsetEnemyIndex;
			}

			if (CInputActionSet.left.isJustPressed() || CInputActionSet.altLeft.isJustPressed()) {
				--offsetEnemyIndex;
			}

			if (CInputActionSet.right.isJustPressed() || CInputActionSet.altRight.isJustPressed()) {
				++offsetEnemyIndex;
			}

			if (offsetEnemyIndex != 0) {
				ArrayList<AbstractMonster> prefiltered = AbstractDungeon.getCurrRoom().monsters.monsters;
				ArrayList<AbstractMonster> sortedMonsters = new ArrayList(AbstractDungeon.getCurrRoom().monsters.monsters);
				Iterator var4 = prefiltered.iterator();

				AbstractMonster m;
				while (var4.hasNext()) {
					m = (AbstractMonster) var4.next();
					if (m.isDying) {
						sortedMonsters.remove(m);
					}
				}

				sortedMonsters.sort(AbstractMonster.sortByHitbox);
				if (sortedMonsters.isEmpty()) {
					return;
				}

				var4 = sortedMonsters.iterator();

				while (var4.hasNext()) {
					m = (AbstractMonster) var4.next();
					if (m.hb.hovered) {
						this.hoveredMonster = m;
						break;
					}
				}

				AbstractMonster newTarget;
				if (this.hoveredMonster == null) {
					if (offsetEnemyIndex == 1) {
						newTarget = (AbstractMonster) sortedMonsters.get(0);
					} else {
						newTarget = (AbstractMonster) sortedMonsters.get(sortedMonsters.size() - 1);
					}
				} else {
					int currentTargetIndex = sortedMonsters.indexOf(this.hoveredMonster);
					int newTargetIndex = currentTargetIndex + offsetEnemyIndex;
					newTargetIndex = (newTargetIndex + sortedMonsters.size()) % sortedMonsters.size();
					newTarget = (AbstractMonster) sortedMonsters.get(newTargetIndex);
				}

				if (newTarget != null) {
					Hitbox target = newTarget.hb;
					Gdx.input.setCursorPosition((int) target.cX, Settings.HEIGHT - (int) target.cY);
					this.hoveredMonster = newTarget;
				}

				if (this.hoveredMonster.halfDead) {
					this.hoveredMonster = null;
				}
			}

		}
	}

	private void updateControllerInput() {
		if (Settings.isControllerMode) {
			if (CInputActionSet.cancel.isJustPressed()) {
				CInputActionSet.cancel.unpress();
				this.close();
			} else {
				if (!this.hbBot.hovered && !this.hbTop.hovered) {
					if (this.potion.canUse()) {
						Gdx.input.setCursorPosition((int) this.hbBot.cX, Settings.HEIGHT - (int) this.hbBot.cY);
					} else {
						Gdx.input.setCursorPosition((int) this.hbTop.cX, Settings.HEIGHT - (int) this.hbTop.cY);
					}
				} else if (this.hbBot.hovered) {
					if (CInputActionSet.up.isJustPressed() || CInputActionSet.down.isJustPressed() || CInputActionSet.altUp.isJustPressed() || CInputActionSet.altDown.isJustPressed()) {
						Gdx.input.setCursorPosition((int) this.hbTop.cX, Settings.HEIGHT - (int) this.hbTop.cY);
					}
				} else if (this.hbTop.hovered && this.potion.canUse() && (CInputActionSet.up.isJustPressed() || CInputActionSet.down.isJustPressed() || CInputActionSet.altUp.isJustPressed() || CInputActionSet.altDown.isJustPressed())) {
					Gdx.input.setCursorPosition((int) this.hbBot.cX, Settings.HEIGHT - (int) this.hbBot.cY);
				}

			}
		}
	}

	private void updateTargetMode() {
		if (InputHelper.justClickedRight || AbstractDungeon.isScreenUp || (float) InputHelper.mY > (float) Settings.HEIGHT - 80.0F * Settings.scale || AbstractDungeon.player.hoveredCard != null || (float) InputHelper.mY < 140.0F * Settings.scale || CInputActionSet.cancel.isJustPressed()) {
			CInputActionSet.cancel.unpress();
			this.targetMode = false;
			GameCursor.hidden = false;
		}

		this.hoveredMonster = null;
		Iterator var1 = AbstractDungeon.getMonsters().monsters.iterator();

		while (var1.hasNext()) {
			AbstractMonster m = (AbstractMonster) var1.next();
			if (m.hb.hovered && !m.isDying) {
				this.hoveredMonster = m;
				break;
			}
		}

		if (InputHelper.justClickedLeft || CInputActionSet.select.isJustPressed()) {
			InputHelper.justClickedLeft = false;
			CInputActionSet.select.unpress();
			if (this.hoveredMonster != null) {
				CardCrawlGame.metricData.potions_floor_usage.add(AbstractDungeon.floorNum);
				this.potion.use(this.hoveredMonster);
				var1 = AbstractDungeon.player.relics.iterator();

				while (var1.hasNext()) {
					AbstractRelic r = (AbstractRelic) var1.next();
					r.onUsePotion();
				}

				GathererMod.potionSack.removePotion(this.slot);

				BaseMod.publishPostPotionUse(this.potion);

				this.targetMode = false;
				GameCursor.hidden = false;
			}
		}

	}

	private void updateInput() {
		if (InputHelper.justClickedLeft) {
			InputHelper.justClickedLeft = false;
			if (this.hbBot.hovered) {
				this.hbBot.clickStarted = true;
			} else if (this.hbTop.hovered) {
				this.hbTop.clickStarted = true;
			} else {
				this.close();
			}
		}

		if (!this.hbBot.clicked && (!this.hbBot.hovered || !CInputActionSet.select.isJustPressed()) || AbstractDungeon.isScreenUp && !(this.potion instanceof FruitJuice)) {
			if ((this.hbTop.clicked || this.hbTop.hovered && CInputActionSet.select.isJustPressed()) && this.potion.canDiscard()) {
				CInputActionSet.select.unpress();
				this.hbTop.clicked = false;
				CardCrawlGame.sound.play("POTION_DROP_2");
				GathererMod.potionSack.removePotion(this.slot);
				this.slot = -1;
				this.potion = null;
				this.close();
			}
		} else {
			CInputActionSet.select.unpress();
			this.hbBot.clicked = false;
			if (this.potion.canUse()) {
				if (this.potion.targetRequired) {
					this.targetMode = true;
					GameCursor.hidden = true;
					this.autoTargetFirst = true;
				} else {
					CardCrawlGame.metricData.potions_floor_usage.add(AbstractDungeon.floorNum);
					this.potion.use((AbstractCreature) null);
					Iterator var1 = AbstractDungeon.player.relics.iterator();

					while (var1.hasNext()) {
						AbstractRelic r = (AbstractRelic) var1.next();
						r.onUsePotion();
					}

					CardCrawlGame.sound.play("POTION_1");
					GathererMod.potionSack.removePotion(this.slot);
					BaseMod.publishPostPotionUse(this.potion);
				}

				this.close();
			}
		}

	}

	public void render(SpriteBatch sb) {
		if (!this.isHidden) {
			sb.setColor(Color.WHITE);
			sb.draw(ImageMaster.POTION_UI_SHADOW, this.x - RAW_W / 2.0F, this.y - RAW_H / 2.0F, RAW_W / 2.0F, RAW_H / 2.0F, RAW_W, RAW_H, Settings.scale, Settings.scale, 0.0F, 0, 0, RAW_W, RAW_H, false, true);
			sb.draw(ImageMaster.POTION_UI_BG, this.x - RAW_W / 2.0F, this.y - RAW_H / 2.0F, RAW_W / 2.0F, RAW_H / 2.0F, RAW_W, RAW_H, Settings.scale, Settings.scale, 0.0F, 0, 0, RAW_W, RAW_H, false, true);
			if (this.hbBot.hovered) {
				sb.draw(ImageMaster.POTION_UI_TOP, this.x - RAW_W / 2.0F, this.y - RAW_H / 2.0F, RAW_W / 2.0F, RAW_H / 2.0F, RAW_W, RAW_H, Settings.scale, Settings.scale, 0.0F, 0, 0, RAW_W, RAW_H, false, true);
			} else if (this.hbTop.hovered) {
				sb.draw(ImageMaster.POTION_UI_MID, this.x - RAW_W / 2.0F, this.y - RAW_H / 2.0F, RAW_W / 2.0F, RAW_H / 2.0F, RAW_W, RAW_H, Settings.scale, Settings.scale, 0.0F, 0, 0, RAW_W, RAW_H, false, true);
			}

			sb.draw(ImageMaster.POTION_UI_OVERLAY, this.x - RAW_W / 2.0F, this.y - RAW_H / 2.0F, RAW_W / 2.0F, RAW_H / 2.0F, RAW_W, RAW_H, Settings.scale, Settings.scale, 0.0F, 0, 0, RAW_W, RAW_H, false, true);
			Color c = Settings.CREAM_COLOR;
			if (!this.potion.canUse() || AbstractDungeon.isScreenUp) {
				c = Color.GRAY;
			}

			if (this.potion instanceof FruitJuice) {
				if (AbstractDungeon.getCurrRoom().event != null) {
					if (!(AbstractDungeon.getCurrRoom().event instanceof WeMeetAgain)) {
						c = Settings.CREAM_COLOR;
					}
				} else {
					c = Settings.CREAM_COLOR;
				}
			}

			String label = DRINK_LABEL;
			if (this.potion.isThrown) {
				label = THROW_LABEL;
			}

			FontHelper.renderFontCenteredWidth(sb, FontHelper.topPanelInfoFont, label, this.x, this.y - 35.0F * Settings.scale, c);
			FontHelper.renderFontCenteredWidth(sb, FontHelper.topPanelInfoFont, DISCARD_LABEL, this.x, this.y + 22.0F * Settings.scale, Color.SALMON);
			this.hbBot.render(sb);
			this.hbTop.render(sb);
			if (this.hbBot.hovered) {
				if (this.potion.isThrown) {
					TipHelper.renderGenericTip(this.x + 124.0F * Settings.scale, this.y + 50.0F * Settings.scale, PotionPopUp.LABEL[0], PotionPopUp.MSG[0]);
				} else {
					TipHelper.renderGenericTip(this.x + 124.0F * Settings.scale, this.y + 50.0F * Settings.scale, PotionPopUp.LABEL[1], PotionPopUp.MSG[1]);
				}
			} else if (this.hbTop.hovered) {
				TipHelper.renderGenericTip(this.x + 124.0F * Settings.scale, this.y + 50.0F * Settings.scale, PotionPopUp.LABEL[2], PotionPopUp.MSG[2]);
			}
		}

		if (this.targetMode) {
			if (this.hoveredMonster != null) {
				this.hoveredMonster.renderReticle(sb);
			}

			this.renderTargetingUi(sb);
		}

	}

	private void renderTargetingUi(SpriteBatch sb) {
		float x = (float) InputHelper.mX;
		float y = (float) InputHelper.mY;
		this.controlPoint = new Vector2(this.potion.posX - (x - this.potion.posX) / 4.0F, y + (y - this.potion.posY - 40.0F * Settings.scale) / 2.0F);
		if (this.hoveredMonster == null) {
			this.arrowScale = Settings.scale;
			this.arrowScaleTimer = 0.0F;
			sb.setColor(new Color(1.0F, 1.0F, 1.0F, 1.0F));
		} else {
			this.arrowScaleTimer += Gdx.graphics.getDeltaTime();
			if (this.arrowScaleTimer > 1.0F) {
				this.arrowScaleTimer = 1.0F;
			}

			this.arrowScale = Interpolation.elasticOut.apply(Settings.scale, Settings.scale * ARROW_TARGET_SCALE, this.arrowScaleTimer);
			sb.setColor(new Color(1.0F, 0.2F, 0.3F, 1.0F));
		}

		Vector2 tmp = new Vector2(this.controlPoint.x - x, this.controlPoint.y - y);
		tmp.nor();
		this.drawCurvedLine(sb, new Vector2(this.potion.posX, this.potion.posY - 40.0F * Settings.scale), new Vector2(x, y), this.controlPoint);
		sb.draw(ImageMaster.TARGET_UI_ARROW, x - TARGET_ARROW_W / 2, y - TARGET_ARROW_W / 2, 128.0F, 128.0F, TARGET_ARROW_W, TARGET_ARROW_W, this.arrowScale, this.arrowScale, tmp.angle() + 90.0F, 0, 0, TARGET_ARROW_W, TARGET_ARROW_W, false, false);
	}

	private void drawCurvedLine(SpriteBatch sb, Vector2 start, Vector2 end, Vector2 control) {
		float radius = 7.0F * Settings.scale;

		for (int i = 0; i < this.points.length - 1; ++i) {
			this.points[i] = (Vector2) Bezier.quadratic(this.points[i], (float) i / 20.0F, start, control, end, new Vector2());
			radius += 0.4F * Settings.scale;
			Vector2 tmp;
			float angle;
			if (i != 0) {
				tmp = new Vector2(this.points[i - 1].x - this.points[i].x, this.points[i - 1].y - this.points[i].y);
				angle = tmp.nor().angle() + 90.0F;
			} else {
				tmp = new Vector2(this.controlPoint.x - this.points[i].x, this.controlPoint.y - this.points[i].y);
				angle = tmp.nor().angle() + 270.0F;
			}

			sb.draw(ImageMaster.TARGET_UI_CIRCLE, this.points[i].x - 64.0F, this.points[i].y - 64.0F, 64.0F, 64.0F, 128.0F, 128.0F, radius / 18.0F, radius / 18.0F, angle, 0, 0, 128, 128, false, false);
		}

	}
}
