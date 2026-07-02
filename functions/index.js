const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");

admin.initializeApp();

const db = admin.firestore();
const messaging = admin.messaging();

function chunkArray(items, size) {
  const chunks = [];

  for (let index = 0; index < items.length; index += size) {
    chunks.push(items.slice(index, index + size));
  }

  return chunks;
}

async function sendToTokens(tokens, payload) {
  const chunks = chunkArray(tokens, 500);

  for (const chunk of chunks) {
    await messaging.sendEachForMulticast({
      tokens: chunk,
      ...payload
    });
  }
}

exports.notifyUsersWhenEventCreated = onDocumentCreated(
  "events/{eventId}",
  async (event) => {
    const snapshot = event.data;

    if (!snapshot) return;

    const data = snapshot.data();

    const creatorUid = data.creatorUid || "";
    const title = data.title || "Nuevo evento";
    const creatorName = data.creatorName || "Un usuario";
    const startDate = data.startDate || "";
    const endDate = data.endDate || "";

    const tokenSnapshots = await db.collectionGroup("tokens").get();

    const tokens = [];

    tokenSnapshots.forEach((tokenDoc) => {
      const tokenData = tokenDoc.data();
      const token = tokenData.token;

      const pathParts = tokenDoc.ref.path.split("/");
      const userId = pathParts[1];

      if (token && userId !== creatorUid) {
        tokens.push(token);
      }
    });

    if (tokens.length === 0) return;

    await sendToTokens(tokens, {
      notification: {
        title: "Nuevo evento creado",
        body: `${creatorName} ha creado: ${title}`
      },
      data: {
        type: "event_created",
        eventId: event.params.eventId,
        title,
        creatorName,
        startDate,
        endDate
      }
    });
  }
);

exports.dailyEventEndingReminders = onSchedule(
  {
    schedule: "0 9 * * *",
    timeZone: "Europe/Madrid"
  },
  async () => {
    const today = new Date();

    function toIsoDate(date) {
      return date.toISOString().slice(0, 10);
    }

    const todayString = toIsoDate(today);

    const maxDate = new Date(today);
    maxDate.setDate(maxDate.getDate() + 15);

    const maxDateString = toIsoDate(maxDate);

    const eventsSnapshot = await db
      .collection("events")
      .where("endDate", ">=", todayString)
      .where("endDate", "<=", maxDateString)
      .get();

    if (eventsSnapshot.empty) return;

    const tokenSnapshots = await db.collectionGroup("tokens").get();

    const allTokens = [];

    tokenSnapshots.forEach((tokenDoc) => {
      const token = tokenDoc.data().token;

      if (token) {
        allTokens.push(token);
      }
    });

    if (allTokens.length === 0) return;

    for (const eventDoc of eventsSnapshot.docs) {
      const data = eventDoc.data();

      await sendToTokens(allTokens, {
        notification: {
          title: "Recordatorio de evento",
          body: `El evento "${data.title}" finaliza pronto: ${data.endDate}`
        },
        data: {
          type: "event_ending_reminder",
          eventId: eventDoc.id,
          title: data.title || "",
          endDate: data.endDate || ""
        }
      });
    }
  }
);
