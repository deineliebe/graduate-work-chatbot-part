import postgres from "postgres";

let sql;

function initSQL() {
    if (sql) return;
    const host = $env.get(`POSTGRES_HOST`);
    const database = $env.get(`DB_NAME`);
    const username = $secrets.get(`POSTGRES_USER`);
    const password = $secrets.get(`POSTGRES_PASSWORD`);
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
        return sql`CREATE TABLE IF NOT EXISTS users (
            id SERIAL PRIMARY KEY,
            channelid VARCHAR(25) UNIQUE
        );`;
    },
    async addUser(channelUserId) {
        await users.createTable();
        return sql`INSERT INTO users(channelid)
            VALUES (${channelUserId})
            ON CONFLICT (channelid) DO UPDATE
            SET channelid = EXCLUDED.channelid;`;
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

const emailData = {
    async createTable() {
        initSQL();
        return sql`CREATE TABLE IF NOT EXISTS authorizationData (
            id INT NOT NULL,
            email VARCHAR(256) NOT NULL UNIQUE PRIMARY KEY,
            password VARCHAR(30) NOT NULL,
            FOREIGN KEY (id) REFERENCES users (id)
        );`;
    },
    async changeEmail(id, email, password) {
        await users.createTable();
        return sql`INSERT INTO authorizationData(id, email, password)
            VALUES (${id}, ${email}, ${password})
            ON CONFLICT (id) DO UPDATE
            SET email = EXCLUDED.email;`;
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
            WHERE name = ${name}, username=${username};`.then(res => { return _.first(res); });
    },
};

const statuses = {
    async createTable() {
        initSQL();
        return sql`CREATE TABLE IF NOT EXISTS statuses (
            status VARCHAR(25) PRIMARY KEY
        );`;
    },
    async getStatuses() {
        await users.createTable();
        return sql`SELECT status FROM statuses;`;
    },
};

export default {
    users,
    emailData,
    tasks,
    statuses
};
