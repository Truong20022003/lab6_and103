const mongoose = require('mongoose');

const local = "mongodb+srv://truong2:truong@cluster0.drtagx5.mongodb.net/lab6";

const connect = async () => {
    try {
        await mongoose.connect(local);
        console.log('Connect success');
    } catch (error) {
        console.error('Connection to MongoDB failed:', error);
    }
}

module.exports = { connect };
