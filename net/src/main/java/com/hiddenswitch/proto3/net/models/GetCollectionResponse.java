package com.hiddenswitch.proto3.net.models;

import com.hiddenswitch.proto3.net.client.models.CardRecord;
import com.hiddenswitch.proto3.net.client.models.InventoryCollection;
import com.hiddenswitch.proto3.net.impl.util.InventoryRecord;
import net.demilich.metastone.game.entities.heroes.HeroClass;

import java.io.Serializable;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by bberman on 1/22/17.
 */
public class GetCollectionResponse implements Serializable {
	private List<GetCollectionResponse> responses;
	private List<InventoryRecord> inventoryRecords;
	private CollectionTypes collectionType;
	private HeroClass heroClass;
	private String name;
	private String collectionId;
	private String userId;

	public static GetCollectionResponse batch(List<GetCollectionResponse> responses) {
		return new GetCollectionResponse()
				.withResponses(responses);
	}

	public static GetCollectionResponse user(String collectionId, List<InventoryRecord> inventoryRecords) {
		return new GetCollectionResponse()
				.withCollectionId(collectionId)
				.withCardRecords(inventoryRecords)
				.withUserId(collectionId)
				.withCollectionType(CollectionTypes.USER);
	}

	public static GetCollectionResponse deck(String userId, String deckId, String name, HeroClass heroClass, List<InventoryRecord> inventoryRecords) {
		return new GetCollectionResponse()
				.withCollectionType(CollectionTypes.DECK)
				.withCollectionId(deckId)
				.withCardRecords(inventoryRecords)
				.withHeroClass(heroClass)
				.withUserId(userId)
				.withName(name);
	}

	public List<InventoryRecord> getInventoryRecords() {
		return inventoryRecords;
	}

	public void setInventoryRecords(List<InventoryRecord> inventoryRecords) {
		this.inventoryRecords = inventoryRecords;
	}

	public HeroClass getHeroClass() {
		return heroClass;
	}

	public void setHeroClass(HeroClass heroClass) {
		this.heroClass = heroClass;
	}

	public GetCollectionResponse withCardRecords(List<InventoryRecord> inventoryRecords) {
		this.inventoryRecords = inventoryRecords;
		return this;
	}

	public GetCollectionResponse withHeroClass(final HeroClass heroClass) {
		this.heroClass = heroClass;
		return this;
	}

	public CollectionTypes getCollectionType() {
		return collectionType;
	}

	public void setCollectionType(CollectionTypes collectionType) {
		this.collectionType = collectionType;
	}

	public GetCollectionResponse withCollectionType(final CollectionTypes collectionType) {
		this.collectionType = collectionType;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public GetCollectionResponse withName(final String name) {
		this.name = name;
		return this;
	}

	public String getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(String collectionId) {
		this.collectionId = collectionId;
	}

	private GetCollectionResponse withCollectionId(String collectionId) {
		this.collectionId = collectionId;
		return this;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public GetCollectionResponse withUserId(String userId) {
		this.userId = userId;
		return this;
	}

	public List<GetCollectionResponse> getResponses() {
		return responses;
	}

	public void setResponses(List<GetCollectionResponse> responses) {
		this.responses = responses;
	}

	public GetCollectionResponse withResponses(final List<GetCollectionResponse> responses) {
		this.responses = responses;
		return this;
	}

	public InventoryCollection asInventoryCollection() {
		if (getResponses() != null
				&& getResponses().size() > 0) {
			throw new RuntimeException();
		}

		String displayName = getCollectionId();

		if (getName() != null) {
			displayName = getName();
		}

		InventoryCollection collection = new InventoryCollection()
				.name(displayName)
				.id(getCollectionId())
				.type(getCollectionType().toString())
				.inventory(getInventoryRecords().stream().map(cr ->
						new CardRecord()
								.userId(cr.getUserId())
								.collectionIds(cr.getCollectionIds())
								.cardDesc(cr.getCardDescMap())
								.id(cr.getId())
								.allianceId(cr.getAllianceId())
								.donorUserId(cr.getDonorUserId()))
						.collect(toList()));

		if (getHeroClass() != null) {
			collection.heroClass(getHeroClass().toString());
		}

		return collection;
	}
}

