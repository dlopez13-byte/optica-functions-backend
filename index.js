const {onDocumentCreated} = require("firebase-functions/v2/firestore");
const {getMessaging} = require("firebase-admin/messaging");
const {getFirestore} = require("firebase-admin/firestore");
const {getAuth} = require("firebase-admin/auth");
const admin = require("firebase-admin");
const express = require('express');

// QA FIX: Inicialización ultra-robusta para Render
if (process.env.FIREBASE_SERVICE_ACCOUNT) {
    const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
    admin.initializeApp({
        credential: admin.credential.cert(serviceAccount),
        projectId: "optica-40088"
    });
    console.log("Firebase inicializado con Service Account desde Env Var");
} else {
    admin.initializeApp({
        projectId: "optica-40088"
    });
    console.log("Firebase inicializado sin Service Account (Riesgo de 401)");
}

const db = getFirestore();
const auth = getAuth();
const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json());

// Logger de auditoría
app.use((req, res, next) => {
    console.log(`[${new Date().toISOString()}] ${req.method} ${req.url}`);
    next();
});

const ADMIN_EMAILS = [
    "padre@optica.com",
    "madre@optica.com",
    "11b.diego.lopez@gmail.com",
    "diego@admin.com"
].map(e => e.toLowerCase());

app.get('/', (req, res) => {
    res.send('Backend Mirada Y Estilo: OPERATIVO');
});

app.post('/login', async (req, res) => {
    try {
        const { idToken } = req.body;
        if (!idToken) return res.status(400).json({ error: "No token" });

        const decodedToken = await auth.verifyIdToken(idToken);
        const email = decodedToken.email.toLowerCase();
        const { uid, name } = decodedToken;

        const role = ADMIN_EMAILS.includes(email) ? "admin" : "cliente";

        await db.collection('users').doc(uid).set({
            email, name: name || "Usuario", role, lastLogin: Date.now()
        }, { merge: true });

        console.log(`Login OK: ${email} (${role})`);
        res.status(200).json({ uid, email, name, role });

    } catch (error) {
        console.error("Fallo de Autenticación:", error.message);
        res.status(401).json({
            success: false,
            message: "Fallo de validación en el servidor: " + error.message
        });
    }
});

// PRODUCTOS
app.get('/productos', async (req, res) => {
    const mockProducts = [
        { id: "1", name: "Ray-Ban Aviator Classic", category: "Sol", imageUrl: "https://images.unsplash.com/photo-1572635196237-14b3f281503f?q=80&w=800", price: 160.00 },
        { id: "2", name: "Oakley Holbrook Prizm", category: "Sol", imageUrl: "https://images.unsplash.com/photo-1511499767390-a7335958648d?q=80&w=800", price: 145.00 }
    ];
    res.json(mockProducts);
});

// PEDIDOS
app.post('/pedidos', async (req, res) => {
    try {
        const docRef = await db.collection('orders').add(req.body);
        res.status(201).json({ success: true, id: docRef.id });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

app.listen(PORT, () => console.log(`Servidor en puerto ${PORT}`));

exports.notifyAdminOnNewOrder = onDocumentCreated("orders/{orderId}", async (event) => {
    const payload = {
        notification: { title: "¡Nuevo Pedido Recibido!", body: "Toca para ver detalles." },
        topic: "admin_orders"
    };
    try { await getMessaging().send(payload); } catch (e) { console.error(e); }
});
