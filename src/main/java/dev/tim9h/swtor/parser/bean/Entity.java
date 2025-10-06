package dev.tim9h.swtor.parser.bean;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Entity {

	private static final Logger LOGGER = LogManager.getLogger(Entity.class);

	private static final Pattern PATTERN_PLAYER = Pattern.compile(
			"^(@)?(.+)#(\\d+)(?:\\/(.+) \\{(\\d+)\\}:(\\d+))?\\|\\(([-0-9]*\\.\\d*),([-0-9]*\\.\\d*),([-0-9]*\\.\\d*),([-0-9]*\\.\\d*)\\)\\|\\((\\d*)\\/(\\d*)\\)$");

	private static final Pattern PATTERN_NPC = Pattern.compile(
			"^ ?(?:(.+) )?\\{(\\d+)\\}:(\\d+)\\|\\(([-0-9]*\\.\\d*),([-0-9]*\\.\\d*),([-0-9]*\\.\\d*),([-0-9]*\\.\\d*)\\)\\|\\((\\d*)\\/(\\d*)\\)$");

	private boolean empty;

	private boolean equals;

	private boolean player;

	private String name;

	private String id;

	private String id2;

	private String companion;

	private String companionId;

	private String companionId2;

	private float coordX;

	private float coordY;

	private float coordA;

	private float coordB;

	private int value0;

	private int value1;

	public Entity(String entity) {
		if (StringUtils.isBlank(entity)) {
			empty = true;
		} else if (Strings.CS.equals(entity, "=")) {
			equals = true;
		} else {
			var matcher = PATTERN_PLAYER.matcher(entity);
			if (matcher.find() && matcher.groupCount() == 12) {
				setPlayer("@".equals(matcher.group(1)));
				setName(matcher.group(2));
				setId(matcher.group(3));
				setCompanion(matcher.group(4));
				setCompanionId(matcher.group(5));
				setCompanionId2(matcher.group(6));
				setCoordX(Float.parseFloat(matcher.group(7)));
				setCoordY(Float.parseFloat(matcher.group(8)));
				setCoordA(Float.parseFloat(matcher.group(9)));
				setCoordB(Float.parseFloat(matcher.group(10)));
				setValue0(Integer.parseInt(matcher.group(11)));
				setValue1(Integer.parseInt(matcher.group(12)));
			} else {
				matcher = PATTERN_NPC.matcher(entity);
				if (matcher.find() && matcher.groupCount() == 9) {
					setName(matcher.group(1));
					setId(matcher.group(2));
					setId2(matcher.group(3));
					setCoordX(Float.parseFloat(matcher.group(4)));
					setCoordY(Float.parseFloat(matcher.group(5)));
					setCoordA(Float.parseFloat(matcher.group(6)));
					setCoordB(Float.parseFloat(matcher.group(7)));
					setValue0(Integer.parseInt(matcher.group(8)));
					setValue1(Integer.parseInt(matcher.group(9)));
				} else {
					LOGGER.warn(() -> "unable to parse entity: " + entity);
				}
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isPlayer() {
		return player;
	}

	public void setPlayer(boolean player) {
		this.player = player;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public float getCoordX() {
		return coordX;
	}

	public void setCoordX(float coordX) {
		this.coordX = coordX;
	}

	public float getCoordY() {
		return coordY;
	}

	public void setCoordY(float coordY) {
		this.coordY = coordY;
	}

	public float getCoordA() {
		return coordA;
	}

	public void setCoordA(float coordA) {
		this.coordA = coordA;
	}

	public float getCoordB() {
		return coordB;
	}

	public void setCoordB(float coordB) {
		this.coordB = coordB;
	}

	public int getValue0() {
		return value0;
	}

	public void setValue0(int value0) {
		this.value0 = value0;
	}

	public int getValue1() {
		return value1;
	}

	public void setValue1(int value1) {
		this.value1 = value1;
	}

	public boolean isEquals() {
		return equals;
	}

	public void setEquals(boolean equals) {
		this.equals = equals;
	}

	public boolean isEmpty() {
		return empty;
	}

	public void setEmpty(boolean empty) {
		this.empty = empty;
	}

	public String getCompanion() {
		return companion;
	}

	public void setCompanion(String companion) {
		this.companion = companion;
	}

	public String getCompanionId() {
		return companionId;
	}

	public void setCompanionId(String companionId) {
		this.companionId = companionId;
	}

	public String getCompanionId2() {
		return companionId2;
	}

	public void setCompanionId2(String companionId2) {
		this.companionId2 = companionId2;
	}

	public String getId2() {
		return id2;
	}

	public void setId2(String id2) {
		this.id2 = id2;
	}

	@Override
	public String toString() {
		if (isEmpty()) {
			return StringUtils.EMPTY;
		} else if (isEquals()) {
			return "=";
		}
		return getName();
	}

}
