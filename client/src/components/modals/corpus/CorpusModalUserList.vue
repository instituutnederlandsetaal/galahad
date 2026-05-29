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
                <GButton :disabled="!newUser.length" @click="setUser(newUser)" title="Add"
                    ><i class="fa fa-plus"></i
                ></GButton>
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
                        <GButton plain @click="userToDelete = userName" v-if="canDelete" title="Remove">
                            <i class="fa fa-close"></i>
                        </GButton>
                    </div>
                </li>
            </ul>
        </td>
    </tr>

    <!-- delete modal -->
    <DeleteModal
        v-if="userToDelete"
        :itemName="userToDelete"
        @hide="userToDelete = null"
        @delete="removeUser(userToDelete)"
    >
        <template #action>remove access for user</template>
    </DeleteModal>
</template>

<script setup lang="ts">
import useCorpora from "@/stores/corpora"

const {
    users = [],
    listName = "Users",
    showAddDialog = true,
} = defineProps<{ users: string[]; listName: string; showAddDialog: boolean }>()

const { canDelete } = storeToRefs(useCorpora())

const newUser = ref<string>("")
const userToDelete = ref<string>()

function setUser(username: string) {
    username = username.trim()
    if (!username) return
    newUser.value = ""
    if (users.includes(username)) return
    users.push(username)
}
function removeUser(username: string) {
    const removeIndex = users.indexOf(username)
    users.splice(removeIndex, 1)
}
</script>

<style scoped lang="scss">
.users {
    display: flex;

    > p {
        margin: 5px 0px;
        flex: 1;
    }
}

ul {
    padding: 0 0 0 20px;
}

.usersInput {
    display: flex;
    gap: 0.5rem;

    > input {
        flex: 1;
    }
}
</style>
