<template>
    <tr>
        <td colspan="2">
            <hr />
        </td>
    </tr>
    <tr v-if="showAddDialog">
        <td>
            <label> {{ listName }}: {{ users.length }}</label>
        </td>
        <td>
            <div class="usersInput">
                <GInput placeholder="CLARIN login (email)" v-model="newUser" @enter="setUser(newUser)" />
                <GButton :disabled="!newUser.length" @click="setUser(newUser)">Add</GButton>
            </div>
        </td>
    </tr>
    <tr v-if="users.length > 0">
        <td>
            <label v-if="!showAddDialog"> {{ listName }}: {{ users.length }}</label>
        </td>
        <td>
            <ul>
                <li v-for="(userName, i) in users" :key="i">
                    <div class="users">
                        <p>{{ userName }}</p>
                        <GButton plain @click="userToDelete = userName" v-if="canDelete(userName)">
                            &#10006;&nbsp;remove
                        </GButton>
                    </div>
                </li>
            </ul>
        </td>
    </tr>

    <!-- delete modal -->
    <DeleteModal v-if="userToDelete" :itemName="userToDelete" @hide="userToDelete = null"
        @delete="removeUser(userToDelete)">
        <template #action>remove access for user</template>
    </DeleteModal>
</template>

<script lang="ts">
import stores from "@/stores"

export default defineComponent({
    name: "UserList",
    props: {
        users: {
            type: Array<string>,
            default: () => []
        },
        listName: {
            type: String,
            default: "Users"
        },
        showAddDialog: {
            type: Boolean,
            default: true
        }
    },
    setup() {
        const userStore = stores.useUser()
        return { userStore: userStore }
    },
    data() {
        return {
            newUser: "",
            userToDelete: null
        }
    },
    methods: {
        canDelete(username: string): boolean {
            if (!this.showAddDialog) {
                return this.userStore.user.id === username
            }
            return true
        },
        setUser(username: string) {
            username = username.trim()
            if (!username) return
            this.newUser = ""
            if (this.users.includes(username)) return
            this.users.push(username)
        },
        removeUser(username: string) {
            const removeIndex = this.users.indexOf(username)
            this.users.splice(removeIndex, 1)
        }
    }
})
</script>

<style scoped lang="scss">
.users {
    display: flex;

    >p {
        margin: 5px 0px;
        flex: 1;
    }
}

ul {
    padding: 0 0 0 20px;
}

.usersInput {
    display: flex;

    >input {
        flex: 1;
    }
}
</style>
