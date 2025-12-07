import 'package:flutter/material.dart';
import 'package:islami_admin/features/hadith/data/repository/hadith_repository.dart';
import 'package:islami_admin/features/hadith/domain/entities/hadith.dart';

class HadithManagementPage extends StatefulWidget {
  const HadithManagementPage({super.key});

  @override
  State<HadithManagementPage> createState() => _HadithManagementPageState();
}

class _HadithManagementPageState extends State<HadithManagementPage> {
  final HadithRepository _hadithRepository = HadithRepository();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Hadith Management')),
      body: StreamBuilder<List<Hadith>>(
        stream: _hadithRepository.getHadiths(),
        builder: (context, snapshot) {
          if (snapshot.hasError) {
            return Center(child: Text('Error: ${snapshot.error}'));
          }
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }
          final hadiths = snapshot.data ?? [];
          return ListView.builder(
            itemCount: hadiths.length,
            itemBuilder: (context, index) {
              final hadith = hadiths[index];
              return Card(
                margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                child: ListTile(
                  title: Text(hadith.text),
                  subtitle: Text(hadith.narrator),
                  trailing: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      IconButton(
                        icon: const Icon(Icons.edit),
                        onPressed: () =>
                            _showAddEditHadithDialog(hadith: hadith),
                      ),
                      IconButton(
                        icon: Icon(
                          Icons.delete,
                          color: Theme.of(context).colorScheme.error,
                        ),
                        onPressed: () => _deleteHadith(hadith.id),
                      ),
                    ],
                  ),
                ),
              );
            },
          );
        },
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => _showAddEditHadithDialog(),
        child: const Icon(Icons.add),
      ),
    );
  }

  void _showAddEditHadithDialog({Hadith? hadith}) {
    final textController = TextEditingController(text: hadith?.text ?? '');
    final narratorController = TextEditingController(
      text: hadith?.narrator ?? '',
    );

    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: Text(hadith == null ? 'Add Hadith' : 'Edit Hadith'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: textController,
                decoration: const InputDecoration(labelText: 'Hadith Text'),
                maxLines: 5,
              ),
              const SizedBox(height: 16),
              TextField(
                controller: narratorController,
                decoration: const InputDecoration(labelText: 'Narrator'),
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: const Text('Cancel'),
            ),
            ElevatedButton(
              onPressed: () {
                final newHadith = Hadith(
                  id:
                      hadith?.id ??
                      DateTime.now().millisecondsSinceEpoch.toString(),
                  text: textController.text,
                  narrator: narratorController.text,
                );
                if (hadith == null) {
                  _addHadith(newHadith);
                } else {
                  _editHadith(newHadith);
                }
                Navigator.of(context).pop();
              },
              child: Text(hadith == null ? 'Add' : 'Update'),
            ),
          ],
        );
      },
    );
  }

  void _addHadith(Hadith hadith) {
    _hadithRepository.addHadith(hadith);
  }

  void _editHadith(Hadith hadith) {
    _hadithRepository.updateHadith(hadith);
  }

  void _deleteHadith(String id) {
    _hadithRepository.deleteHadith(id);
  }
}
