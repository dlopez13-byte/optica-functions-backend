const admin = require("firebase-admin");
const { getFirestore } = require("firebase-admin/firestore");
const { getAuth } = require("firebase-admin/auth");
const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { getMessaging } = require("firebase-admin/messaging");
const express = require('express');

// Lógica de inicialización robusta para entornos fuera de Google Cloud (Render)
let adminConfig = {
    projectId: "optica-40088"
};

if (process.env.GOOGLE_APPLICATION_CREDENTIALS_JSON) {
    try {
        const serviceAccount = JSON.parse(process.env.GOOGLE_APPLICATION_CREDENTIALS_JSON);
        adminConfig.credential = admin.credential.cert(serviceAccount);
        console.log("✅ CONFIG: Firebase inicializado con Service Account (GOOGLE_APPLICATION_CREDENTIALS_JSON).");
    } catch (e) {
        console.error("❌ ERROR: GOOGLE_APPLICATION_CREDENTIALS_JSON no tiene un formato JSON válido.", e.message);
    }
} else {
    console.error("❌ CRÍTICO: Falta la variable de entorno GOOGLE_APPLICATION_CREDENTIALS_JSON en Render.");
}

admin.initializeApp(adminConfig);

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

// Lista oficial de administradores (Roles RBAC)
const ADMIN_EMAILS = [
    "padre@optica.com",
    "madre@optica.com",
    "11b.diego.lopez@gmail.com",
    "diego@admin.com"
].map(e => e.toLowerCase());

app.get('/', (req, res) => {
    res.send('Backend Mirada Y Estilo: OPERATIVO');
});

// Endpoint de Autenticación y Roles
app.post('/login', async (req, res) => {
    try {
        const { idToken } = req.body;
        if (!idToken) return res.status(400).json({ error: "No token provided" });

        // Verificación de integridad del token
        const decodedToken = await auth.verifyIdToken(idToken);
        const email = decodedToken.email.toLowerCase();
        const { uid, name } = decodedToken;

        // Lógica de asignación de roles
        const role = ADMIN_EMAILS.includes(email) ? "admin" : "cliente";

        // Persistencia de perfil en la nube
        const userRef = db.collection('users').doc(uid);
        await userRef.set({
            email,
            name: name || "Usuario de Óptica",
            role,
            lastLogin: Date.now()
        }, { merge: true });

        console.log(`LOGIN EXITOSO: ${email} | ROL: ${role.toUpperCase()}`);
        res.status(200).json({ uid, email, name, role });

    } catch (error) {
        console.error("ERROR DE AUTENTICACIÓN:", error.message);
        res.status(401).json({
            success: false,
            message: "Fallo de validación en el servidor: " + error.message
        });
    }
});

// CATÁLOGO DE PRODUCTOS
app.get('/productos', async (req, res) => {
    try {
        const mockProducts = [
            { id: "1", name: "Ray-Ban Aviator Classic", category: "Sol", imageUrl: "https://images.unsplash.com/photo-1572635196237-14b3f281503f?q=80&w=800", price: 160.00 },
            { id: "2", name: "Oakley Holbrook Prizm", category: "Sol", imageUrl: "https://images.unsplash.com/photo-1511499767390-a7335958648d?q=80&w=800", price: 145.00 },
            { id: "3", name: "Titanium Minimalist", category: "Recetadas", imageUrl: "https://images.unsplash.com/photo-1577803645773-f96470509666?q=80&w=800", price: 210.00 }
        ];
        res.status(200).json(mockProducts);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// REGISTRO DE PEDIDOS
app.post('/pedidos', async (req, res) => {
    try {
        const docRef = await db.collection('orders').add(req.body);
        res.status(201).json({ success: true, id: docRef.id });
    } catch (error) {
        res.status(500).json({ success: false, error: error.message });
    }
});

// HISTORIAL POR USUARIO
app.get('/pedidos/:userId', async (req, res) => {
    try {
        const userId = req.params.userId;
        const snapshot = await db.collection('orders').where('userId', '==', userId).get();
        const orders = [];
        snapshot.forEach(doc => orders.push({ id: doc.id, ...doc.data() }));
        res.status(200).json(orders);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

app.listen(PORT, () => {
    console.log(`🚀 Servidor escuchando en el puerto ${PORT}`);
});

// TRIGGER DE NOTIFICACIONES PUSH
exports.notifyAdminOnNewOrder = onDocumentCreated("orders/{orderId}", async (event) => {
    const orderId = event.params.orderId;
    const payload = {
        notification: {
            title: "¡Nuevo Pedido Recibido!",
            body: "Toca aquí para revisar la receta y los detalles.",
        },
        topic: "admin_orders",
        data: { orderId: orderId, type: "NEW_ORDER" }
    };
    try {
        await getMessaging().send(payload);
        console.log(`🔔 Notificación enviada para el pedido: ${orderId}`);
    } catch (e) {
        console.error("❌ Error enviando notificación push:", e);
    }
});
