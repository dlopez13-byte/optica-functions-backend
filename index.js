const {onDocumentCreated} = require("firebase-functions/v2/firestore");
const {getMessaging} = require("firebase-admin/messaging");
const {getFirestore} = require("firebase-admin/firestore");
const {getAuth} = require("firebase-admin/auth");
const {initializeApp} = require("firebase-admin/app");
const express = require('express');

initializeApp();
const db = getFirestore();
const auth = getAuth();
const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json());

// Logger middleware para QA
app.use((req, res, next) => {
    console.log(`${new Date().toISOString()} - ${req.method} ${req.url}`);
    next();
});

// Lista de correos administradores
const ADMIN_EMAILS = ["padre@optica.com", "madre@optica.com", "diego@admin.com"];

// --- RUTAS DE RENDER ---

app.get('/', (req, res) => {
    res.send('Backend de la Óptica corriendo con éxito en Render');
});

// LOGIN Y ROLES (RBAC) - Soporta /login y /login/
const loginHandler = async (req, res) => {
    try {
        const { idToken } = req.body;
        if (!idToken) return res.status(400).json({error: "No token provided"});

        const decodedToken = await auth.verifyIdToken(idToken);
        const { email, uid, name } = decodedToken;

        let role = ADMIN_EMAILS.includes(email) ? "admin" : "cliente";

        const userRef = db.collection('users').doc(uid);
        await userRef.set({
            email,
            name: name || "Usuario de Óptica",
            role,
            lastLogin: Date.now()
        }, { merge: true });

        console.log(`Login exitoso: ${email} como ${role}`);
        res.status(200).json({ uid, email, name, role });
    } catch (error) {
        console.error("Error en login:", error);
        res.status(401).json({ error: error.message });
    }
};

app.post('/login', loginHandler);
app.post('/login/', loginHandler);

// CATÁLOGO
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

// PEDIDOS
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
        const snapshot = await db.collection('orders')
            .where('userId', '==', userId)
            .orderBy('timestamp', 'desc')
            .get();
        const orders = [];
        snapshot.forEach(doc => orders.push({ id: doc.id, ...doc.data() }));
        res.status(200).json(orders);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

app.listen(PORT, () => {
    console.log(`Servidor escuchando en el puerto ${PORT}`);
});

// --- CLOUD FUNCTION PARA NOTIFICACIONES ---
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
        console.log("Push enviado:", orderId);
    } catch (e) {
        console.error("Error en push:", e);
    }
});
