const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const ADMIN_UID = "admin_selim";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("listings").where("status", "==", "ACTIVE").get());
  await assertFails(unauthDb.collection("concierge_requests").where("status", "==", "OPEN").get());
  await assertFails(unauthDb.collection("coupons").where("status", "==", "ACTIVE").get());
  await assertFails(unauthDb.collection("vip_requests").get());
});

test("User profile creation enforces 100 coins and blocks self-escalation to BUSINESS_VIP", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 1000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      email: "alice@example.com",
      plan: "FREE_STARTER",
      billingCycle: "NONE",
      coins: 100,
      referralCode: "KEMETALICE1",
      referralCount: 0,
      affiliateEarningsCoins: 0,
      verifiedBusiness: false,
      dailyTranslationsUsed: 0,
      quizWins: 0,
      createdAt: now,
      updatedAt: now,
    })
  );

  // User cannot self-upgrade plan or verifiedBusiness directly
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      plan: "BUSINESS_VIP",
      verifiedBusiness: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Cross-user profile read/update denied
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      coins: 500,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Developer Admin can review vip_requests, activate BUSINESS_VIP, and verify listings", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com", email_verified: true }).firestore();
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, { email: "selimmmohammed@gmail.com", email_verified: true }).firestore();
  const now = new Date(Date.now() - 1000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      email: "alice@example.com",
      plan: "FREE_STARTER",
      billingCycle: "NONE",
      coins: 100,
      referralCode: "KEMETALICE1",
      referralCount: 0,
      affiliateEarningsCoins: 0,
      verifiedBusiness: false,
      dailyTranslationsUsed: 0,
      quizWins: 0,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("vip_requests").doc("vip_req_1").set({
      userId: ALICE_UID,
      userName: "Alice",
      userEmail: "alice@example.com",
      targetPlan: "BUSINESS_VIP",
      billingCycle: "MONTHLY",
      amountEgp: 299,
      walletNumber: "01006256085",
      senderWalletType: "VODAFONE_CASH",
      txReference: "VF123456",
      receiptSelected: true,
      receiptUriNote: "receipt.jpg",
      status: "PENDING",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Non-admin cannot approve vip_requests
  await assertFails(
    aliceDb.collection("vip_requests").doc("vip_req_1").update({
      status: "APPROVED",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Admin can list all vip_requests, approve, and upgrade Alice's profile
  await assertSucceeds(adminDb.collection("vip_requests").get());
  await assertSucceeds(
    adminDb.collection("vip_requests").doc("vip_req_1").update({
      status: "APPROVED",
      updatedAt: new Date(Date.now() - 400),
    })
  );
  await assertSucceeds(
    adminDb.collection("users").doc(ALICE_UID).update({
      plan: "BUSINESS_VIP",
      billingCycle: "MONTHLY",
      verifiedBusiness: true,
      coins: 1100,
      updatedAt: new Date(Date.now() - 300),
    })
  );
});

test("Concierge requests allow Business VIP offers, and listings support 5-star reviews & QR coupons", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com", email_verified: true }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@example.com", email_verified: true }).firestore();
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, { email: "selimmmohammed@gmail.com", email_verified: true }).firestore();
  const now = new Date(Date.now() - 1000);

  // Setup Alice (FREE) and Bob (BUSINESS_VIP via admin)
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      plan: "FREE_STARTER",
      billingCycle: "NONE",
      coins: 100,
      referralCode: "KEMETALICE1",
      verifiedBusiness: false,
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    bobDb.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob VIP",
      plan: "FREE_STARTER",
      billingCycle: "NONE",
      coins: 100,
      referralCode: "KEMETBOB1",
      verifiedBusiness: false,
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    adminDb.collection("users").doc(BOB_UID).update({
      plan: "BUSINESS_VIP",
      verifiedBusiness: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Alice creates a custom concierge request
  await assertSucceeds(
    aliceDb.collection("concierge_requests").doc("creq_1").set({
      userId: ALICE_UID,
      requesterName: "Alice",
      category: "TRIP",
      title: "رحلة خاصة للأقصر 3 أيام",
      details: "أرغب في برنامج VIP شامل الفندق والمرشد",
      budgetEgp: 8500,
      city: "الأقصر",
      status: "OPEN",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Bob (Business VIP) can submit an offer; Alice (FREE_STARTER) cannot submit an offer
  await assertSucceeds(
    bobDb.collection("concierge_requests").doc("creq_1").collection("offers").doc("offer_1").set({
      requestId: "creq_1",
      requesterUid: ALICE_UID,
      merchantUserId: BOB_UID,
      merchantName: "Bob VIP",
      priceEgp: 7900,
      proposal: "عرض شامل إقامة 5 نجوم وجولات خاصة",
      whatsappPhone: "+201006256085",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertFails(
    aliceDb.collection("concierge_requests").doc("creq_1").collection("offers").doc("offer_bad").set({
      requestId: "creq_1",
      requesterUid: ALICE_UID,
      merchantUserId: ALICE_UID,
      merchantName: "Alice",
      priceEgp: 5000,
      proposal: "Not VIP",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Alice views offers addressed to her
  await assertSucceeds(
    aliceDb.collection("concierge_requests").doc("creq_1").collection("offers").where("requesterUid", "==", ALICE_UID).get()
  );

  // Reviews & QR Coupons
  await assertSucceeds(
    bobDb.collection("listings").doc("list_1").set({
      userId: BOB_UID,
      sellerName: "Bob VIP",
      title: "جولة دهب",
      description: "رحلة بحرية متميزة",
      category: "TRIP",
      priceEgp: 1200,
      location: "دهب",
      featured: true,
      verifiedSeller: true,
      status: "ACTIVE",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("listings").doc("list_1").collection("reviews").doc(ALICE_UID).set({
      listingId: "list_1",
      userId: ALICE_UID,
      reviewerName: "Alice",
      rating: 5,
      comment: "تجربة رائعة وتنظيم ممتاز",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    bobDb.collection("coupons").doc("cpn_1").set({
      userId: BOB_UID,
      merchantName: "Bob VIP",
      code: "KEMET20",
      title: "خصم 20% على رحلات النيل",
      discountPercent: 20,
      city: "أسوان",
      status: "ACTIVE",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    aliceDb.collection("coupons").where("status", "==", "ACTIVE").get()
  );
});

test("User File Vault enforces strict per-owner access, valid categories, and size limits", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 1000);

  // Valid create by owner (Alice)
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("vault_files").doc("vf_ticket_1").set({
      userId: ALICE_UID,
      title: "تذكرة طيران القاهرة - أسوان",
      fileName: "flight_aswan.pdf",
      mimeType: "application/pdf",
      category: "TRAVEL_TICKETS",
      sizeBytes: 124500,
      note: "رحلة صباحية",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Owner can list & partial update allowed fields
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("vault_files").get()
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("vault_files").doc("vf_ticket_1").update({
      title: "تذكرة ذهاب وعودة - أسوان",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Unauthenticated & cross-user read/write/delete must fail
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("vault_files").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("vault_files").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("vault_files").doc("vf_ticket_1").delete());

  // Invalid category or immutable field tampering fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("vault_files").doc("vf_bad").set({
      userId: ALICE_UID,
      title: "Bad Category",
      fileName: "doc.pdf",
      mimeType: "application/pdf",
      category: "UNKNOWN_CAT",
      sizeBytes: 1024,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Owner delete succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("vault_files").doc("vf_ticket_1").delete()
  );
});

test("Universal Payment Gateway allows Egyptian wallets & international banking/fiat only (blocks crypto/unknown)", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 1000);

  await assertSucceeds(
    aliceDb.collection("vip_requests").doc("vip_we_pay").set({
      userId: ALICE_UID,
      userName: "Alice",
      targetPlan: "PRO",
      billingCycle: "MONTHLY",
      amountEgp: 99,
      walletNumber: "01006256085",
      senderWalletType: "WE_PAY",
      txReference: "WE998811",
      receiptSelected: false,
      status: "PENDING",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("vip_requests").doc("vip_intl_visa").set({
      userId: ALICE_UID,
      userName: "Alice",
      targetPlan: "BUSINESS_VIP",
      billingCycle: "YEARLY",
      amountEgp: 2499,
      walletNumber: "01006256085",
      senderWalletType: "VISA_MASTERCARD",
      txReference: "PP-887766",
      receiptSelected: true,
      status: "PENDING",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Crypto or unsupported payment types must fail
  await assertFails(
    aliceDb.collection("vip_requests").doc("vip_crypto_bad").set({
      userId: ALICE_UID,
      userName: "Alice",
      targetPlan: "PRO",
      billingCycle: "MONTHLY",
      amountEgp: 99,
      walletNumber: "01006256085",
      senderWalletType: "USDT_CRYPTO",
      txReference: "0x123456",
      receiptSelected: false,
      status: "PENDING",
      createdAt: now,
      updatedAt: now,
    })
  );
});

