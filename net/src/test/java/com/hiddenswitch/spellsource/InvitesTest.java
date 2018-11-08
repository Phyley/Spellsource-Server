package com.hiddenswitch.spellsource;

import com.github.fromage.quasi.fibers.Suspendable;
import com.github.fromage.quasi.strands.Strand;
import com.github.fromage.quasi.strands.concurrent.CountDownLatch;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.util.UnityClient;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.hiddenswitch.spellsource.util.Sync.invoke;
import static org.junit.Assert.*;

public class InvitesTest extends SpellsourceTestBase {

	@Test
	@Suspendable
	public void testFriendInvite(TestContext testContext) {
		sync(() -> {
			// Regular friending.
			CountDownLatch friended = new CountDownLatch(1);
			CountDownLatch didReceiveFriend = new CountDownLatch(1);
			AtomicInteger inviteChecks = new AtomicInteger(2);
			AtomicReference<String> recipientId = new AtomicReference<>();
			UnityClient sender = new UnityClient(testContext) {
				@Override
				protected void handleMessage(Envelope env) {
					if (env.getAdded() != null && env.getAdded().getFriend() != null) {
						assertNotNull(env.getAdded().getFriend().getFriendId());
						assertEquals(env.getAdded().getFriend().getFriendId(), recipientId.get());
						didReceiveFriend.countDown();
					}

					if (env.getAdded() != null && env.getAdded().getInvite() != null) {
						Invite invite = env.getAdded().getInvite();
						// Confirm the invite statuses are updated correctly
						if (didReceiveFriend.getCount() == 1L && friended.getCount() == 1L) {
							assertEquals(Invite.StatusEnum.UNDELIVERED, invite.getStatus());
							inviteChecks.decrementAndGet();
						} else if (friended.getCount() == 0L) {
							assertEquals(Invite.StatusEnum.ACCEPTED, invite.getStatus());
							inviteChecks.decrementAndGet();
						}
					}
				}
			};
			sender.ensureConnected();

			UnityClient recipient = new UnityClient(testContext) {
				@Override
				protected void handleMessage(Envelope env) {
					if (env.getAdded() != null && env.getAdded().getInvite() != null) {
						Invite invite = env.getAdded().getInvite();
						try {
							if (invite.getFriendId() != null) {
								assertEquals(Invite.StatusEnum.PENDING, invite.getStatus());
								this.getApi().acceptInvite(invite.getId(), new AcceptInviteRequest());
								friended.countDown();
							}
						} catch (Exception ex) {
							fail(ex.getMessage());
						}
					}
				}
			};
			recipient.ensureConnected();

			sender.createUserAccount();
			recipient.createUserAccount();
			recipientId.set(recipient.getUserId().toString());
			assertTrue(recipient.getAccount().getName().contains("#"));

			InviteResponse inviteResponse = invoke(sender.getApi()::postInvite, new InvitePostRequest()
					.friend(true)
					// Get name contains the privacy token
					.toUserNameWithToken(recipient.getAccount().getName())
					.message("Would you be my friend?"));

			assertEquals(Invite.StatusEnum.UNDELIVERED, inviteResponse.getInvite().getStatus());
			friended.await();
			didReceiveFriend.await();
			Strand.sleep(1000L);
			assertEquals(0, inviteChecks.get());
		});
	}
}
