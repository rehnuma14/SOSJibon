package com.example.sosjibon.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyContactsScreen(
    vm: SettingsViewModel = viewModel(),
    onBack: () -> Unit
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    val primaryGreen = Color(0xFF159A6C)
    val background = Color(0xFFF8FCFA)
    val textDark = Color(0xFF17332A)
    val textGray = Color(0xFF6B7C75)
    val emergencyRed = Color(0xFFD92D20)

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Emergency Contacts",
                        fontWeight = FontWeight.Bold,
                        color = textDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = primaryGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = primaryGreen,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Contact")
            }
        },
        containerColor = background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = emergencyRed)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(color = Color.White, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = emergencyRed,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Emergency Broadcast List",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Contacts stored here receive your GPS location during one-tap SOS alerts.",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Text(
                text = "Saved Contacts (${state.emergencyContacts.size})",
                color = textDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            if (state.emergencyContacts.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(text = "No emergency contacts added yet. Tap + below to add.", color = textGray, fontSize = 13.sp)
                    }
                }
            } else {
                state.emergencyContacts.forEach { contact ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFFFFEBEE), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = emergencyRed, modifier = Modifier.size(24.dp))
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = contact.name, fontWeight = FontWeight.Bold, color = textDark, fontSize = 16.sp)
                                Text(text = "${contact.phone} • ${contact.relation}", fontWeight = FontWeight.SemiBold, color = primaryGreen, fontSize = 13.sp)
                                Text(text = "Blood Group: ${contact.bloodGroup} • Gender: ${contact.gender}", color = textGray, fontSize = 11.5.sp)
                            }

                            IconButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}"))
                                    try {
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                }
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Call", tint = primaryGreen)
                            }

                            IconButton(onClick = { vm.deleteEmergencyContact(contact.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = emergencyRed)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showAddDialog) {
        AddEmergencyContactModal(
            onDismiss = { showAddDialog = false },
            onAdd = { newContact ->
                vm.addEmergencyContact(newContact)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddEmergencyContactModal(
    onDismiss: () -> Unit,
    onAdd: (EmergencyContactItem) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("O+") }
    var gender by remember { mutableStateOf("Male") }
    var relation by remember { mutableStateOf("Family") }

    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    val genders = listOf("Male", "Female", "Other")
    val relations = listOf("Family", "Parent", "Spouse", "Sibling", "Friend", "Doctor", "Other")

    var bloodExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }
    var relationExpanded by remember { mutableStateOf(false) }

    val primaryGreen = Color(0xFF159A6C)
    val textDark = Color(0xFF17332A)
    val textGray = Color(0xFF6B7C75)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add Emergency Contact", fontWeight = FontWeight.Bold, color = textDark) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(text = "Contact Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedLabelColor = primaryGreen)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(text = "Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedLabelColor = primaryGreen)
                )

                Box {
                    OutlinedTextField(
                        value = bloodGroup,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(text = "Blood Group") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, Modifier.clickable { bloodExpanded = true }) },
                        modifier = Modifier.fillMaxWidth().clickable { bloodExpanded = true },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedLabelColor = primaryGreen)
                    )
                    DropdownMenu(expanded = bloodExpanded, onDismissRequest = { bloodExpanded = false }) {
                        bloodGroups.forEach { bg ->
                            DropdownMenuItem(text = { Text(text = bg) }, onClick = { bloodGroup = bg; bloodExpanded = false })
                        }
                    }
                }

                Box {
                    OutlinedTextField(
                        value = gender,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(text = "Gender") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, Modifier.clickable { genderExpanded = true }) },
                        modifier = Modifier.fillMaxWidth().clickable { genderExpanded = true },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedLabelColor = primaryGreen)
                    )
                    DropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                        genders.forEach { g ->
                            DropdownMenuItem(text = { Text(text = g) }, onClick = { gender = g; genderExpanded = false })
                        }
                    }
                }

                Box {
                    OutlinedTextField(
                        value = relation,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(text = "Relation") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, Modifier.clickable { relationExpanded = true }) },
                        modifier = Modifier.fillMaxWidth().clickable { relationExpanded = true },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryGreen, focusedLabelColor = primaryGreen)
                    )
                    DropdownMenu(expanded = relationExpanded, onDismissRequest = { relationExpanded = false }) {
                        relations.forEach { r ->
                            DropdownMenuItem(text = { Text(text = r) }, onClick = { relation = r; relationExpanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank() && phone.isNotBlank(),
                onClick = {
                    onAdd(
                        EmergencyContactItem(
                            name = name.trim(),
                            phone = phone.trim(),
                            bloodGroup = bloodGroup,
                            gender = gender,
                            relation = relation
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
            ) {
                Text(text = "Add Contact")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = "Cancel", color = textGray) }
        }
    )
}
