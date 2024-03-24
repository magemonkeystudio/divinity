package com.promcteam.divinity.manager.effects;

public enum IEffectType {

    DEFAULT_GOOD(true),
    DEFAULT_BAD(false),

    CONTROL_DEFAULT(false),
    CONTROL_ROOT(false),
    CONTROL_STUN(false),
    CONTROL_FEAR(false),

    HARM_DEFAULT(false),
    HARM_BLEED(false),

    INC_STAT_DEFAULT(true),
    INC_STAT_DAMAGE(true),
    INC_STAT_DEFENSE(true),
    INC_STAT_ITEM(true),

    DEC_STAT_DEFAULT(false),
    DEC_STAT_DAMAGE(false),
    DEC_STAT_DEFENSE(false),
    DEC_STAT_ITEM(false),

    ADJUST_STAT(true),

    RESIST(true),

    DISARM(false),
    SILENCE(false),
    INVULNERABILITY(true),
    INVISIBILITY(true),
    UNTARGETABLE(true),
    ANTI_FALL(true),
    ;

    private boolean isPositive;

    private IEffectType(boolean isPositive) {
        this.isPositive = isPositive;
    }

    public boolean isPositive() {
        return this.isPositive;
    }
}
