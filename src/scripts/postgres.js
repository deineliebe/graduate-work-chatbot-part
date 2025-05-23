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
            channel_id VARCHAR(25) UNIQUE
        );`;
    },
    async addUser(channelUserId) {
        await users.createTable();
        return sql`INSERT INTO users(channel_id)
            VALUES (${channelUserId})
            ON CONFLICT (channel_id) DO UPDATE
            SET channel_id = EXCLUDED.channel_id;`;
    },
    async getUsers() {
        await users.createTable();
        return sql`SELECT * FROM users;`;
    },
    async getUser(channelId) {
        await users.createTable();
        return sql`SELECT * FROM users
            WHERE channel_id = ${channelId};`.then(res => { return _.first(res); });;
    },
};

const emailData = {
    async createTable() {
        initSQL();
        return sql`CREATE TABLE IF NOT EXISTS authorization_data (
            id INT NOT NULL UNIQUE,
            email VARCHAR(256) NOT NULL UNIQUE PRIMARY KEY,
            password VARCHAR(30) NOT NULL,
            FOREIGN KEY (id) REFERENCES users (id)
        );`;
    },
    async changeEmail(id, email, password) {
        await users.createTable();
        return sql`INSERT INTO authorization_data(id, email, password)
            VALUES (${id}, ${email}, ${password})
            ON CONFLICT (id) DO UPDATE
            SET email = EXCLUDED.email;`;
    },
};

const userTasks = {
    async createTable() {
        initSQL();
        return sql`CREATE TABLE IF NOT EXISTS user_tasks (
            user_id INT NOT NULL,
            task_id INT NOT NULL,
            FOREIGN KEY (user_id) REFERENCES users (id),
            FOREIGN KEY (task_id) REFERENCES tasks (id)
        );`;
    },
    async addTaskToAUser(userId, taskId) {
        await userTasks.createTable();
        return sql`INSERT INTO user_tasks(user_id, task_id)
            VALUES (${userId}, ${taskId});`;
    }
};

const tasks = {
    async createTable() {
        initSQL();
        return sql`CREATE TABLE IF NOT EXISTS tasks(
            name VARCHAR(150),
            deadline VARCHAR(100),
            username VARCHAR(50),
            status VARCHAR(50),
	        created_at DATE,
            CONSTRAINT task_id PRIMARY KEY (name, username)
        );`;
    },
    async addTask(name, description, deadline, createdAt, status) {
        await tasks.createTable();
        await userTasks.createTable();
        return sql`INSERT INTO tasks(name, description, deadline, created_at, status)
            VALUES (${name}, ${description}, ${deadline}, ${createdAt}, ${status});`;
    },
    async getUserTasksOrderedByCreatedDate(userId) {
        await tasks.createTable();
        await userTasks.createTable();
        return sql`SELECT task_id AS id, name, description, deadline, created_at, status
            FROM user_tasks INNER JOIN tasks
            ON user_tasks.task_id = tasks.id
            WHERE user_id = ${userId}
            ORDER BY created_at DESC;`;
    },
    async getUserLastTaskId(name) {
        await tasks.createTable();
        await userTasks.createTable();
        return sql`SELECT id FROM tasks WHERE name = ${name}
            ORDER BY created_at DESC LIMIT 1;`
            .then(res => { return _.first(res); });
    },
    async getUserTasksOrderedByDeadline(userId) {
        await tasks.createTable();
        await userTasks.createTable();
        return sql`SELECT task_id AS id, name, description, deadline, created_at, status
            FROM user_tasks INNER JOIN tasks
            ON user_tasks.task_id = tasks.id
            WHERE user_id = ${userId} AND deadline >= CURRENT_DATE
            ORDER BY deadline ASC;`;
    },
    async getTasksWithSpecificStatus(userId, status) {
        await tasks.createTable();
        await userTasks.createTable();
        return sql`SELECT task_id AS id, name, description, deadline, created_at, status
            FROM user_tasks INNER JOIN tasks
            ON user_tasks.task_id = tasks.id
            WHERE user_id = ${userId} AND status = ${status}
            ORDER BY created_at DESC;`;
    },
    async getTask(id) {
        await tasks.createTable();
        return sql`SELECT * FROM tasks
            WHERE id = ${id};`.then(res => { return _.first(res); });
    },
    async updateName(id, name) {
        await tasks.createTable();
        return sql`UPDATE tasks SET name = ${name} WHERE id = ${id};`;
    },
    async updateDescription(id, description) {
        await tasks.createTable();
        return sql`UPDATE tasks SET description = ${description} WHERE id = ${id};`;
    },
    async updateDeadline(id, deadline) {
        await tasks.createTable();
        return sql`UPDATE tasks SET deadline = ${deadline} WHERE id = ${id};`;
    },
    async updateStatus(id, status) {
        await tasks.createTable();
        return sql`UPDATE tasks SET status = ${status} WHERE id = ${id};`;
    },
    async deleteTask(id) {
        await tasks.createTable();
        return sql`WITH deleted_parent AS (
                DELETE FROM tasks
                WHERE id = ${id}
                RETURNING id
            )
            DELETE FROM user_tasks
            WHERE task_id IN (SELECT id FROM deleted_parent);`;
    },
    async getStatuses(id) {
        await tasks.createTable();
        return sql`SELECT status
            FROM user_tasks LEFT JOIN tasks
            ON user_tasks.task_id = tasks.id
            WHERE user_id = ${id}
            GROUP BY status;`
            .then(res => { return _.pluck(res, 'status'); });
    }
};

export default {
    users,
    emailData,
    userTasks,
    tasks
};
