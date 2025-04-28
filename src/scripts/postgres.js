import postgres from "postgres";

let sql;

function initSQL() {
    if (sql) return;
    const mode = $env.get("DB_MODE", "TEST");
    const host = $env.get(`POSTGRES_HOST_${mode}`);
    const database = $env.get(`DB_NAME_${mode}`);
    const username = $secrets.get(`POSTGRES_USER_${mode}`);
    const password = $secrets.get(`POSTGRES_PASSWORD_${mode}`);
    sql = postgres({
        host: host,
        port: 5432,
        database: database,
        username: username,
        password: password,
        max_lifetime: null,
        idle_timeout: 0,
        connect_timeout: 30
    });
}


const users = {
    async createTable() {
        initSQL();
        return sql`CREATE TABLE users (
            id SERIAL PRIMARY KEY,
            channelid VARCHAR(25) UNIQUE
        );`;
    },
    async addUser(channelUserId) {
        await users.createTable();
        return sql`INSERT INTO users(channel_user_id)
            VALUES (${channelUserId})
            ON CONFLICT (channelUserId) DO UPDATE
            SET channel_user_id = EXCLUDED.channel_user_id;`;
    },
    async getUsers() {
        await users.createTable();
        return sql`SELECT * FROM users;`;
    },
    async getUser(channelid) {
        await users.createTable();
        return sql`SELECT * FROM users
            WHERE channelid = ${channelid};`.then(res => { return _.first(res); });;
    },
};

const tasks = {
    async createTable() {
        initSQL();
        return sql`CREATE TABLE IF NOT EXISTS tasks(
            name VARCHAR(150),
            deadline VARCHAR(100),
            username VARCHAR(50),
            status VARCHAR(50),
            CONSTRAINT task_id PRIMARY KEY (name, username)
        );`;
    },
    async addTask(name, deadline, username, status) {
        await users.createTable();
        return sql`INSERT INTO tasks(name, deadline, username, status)
            VALUES (${name}, ${deadline}, ${username}, ${status})
            ON CONFLICT (username) DO NOTHING;`;
    },
    async getTasks() {
        await users.createTable();
        return sql`SELECT * FROM tasks;`;
    },
    async getTasksWithSpecificStatus(status) {
        await users.createTable();
        return sql`SELECT * FROM tasks
            WHERE status=${status};`;
    },
    async getTask(name, username) {
        await users.createTable();
        return sql`SELECT * FROM tasks
            WHERE name = ${name}, username=${username};`.then(res => { return _.first(res); });;
    },
};

export default {
    users,
    tasks
};
