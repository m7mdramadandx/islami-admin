import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:islami_admin/core/utils/colors.dart';
import 'package:islami_admin/features/azkar/presentation/bloc/azkar_bloc.dart';
import 'package:islami_admin/features/azkar/presentation/bloc/azkar_event.dart';
import 'package:islami_admin/features/azkar/presentation/bloc/azkar_state.dart';
import 'package:islami_admin/features/home/presentation/widgets/custom_drawer.dart';

class AzkarPage extends StatefulWidget {
  const AzkarPage({super.key});

  @override
  State<AzkarPage> createState() => _AzkarPageState();
}

class _AzkarPageState extends State<AzkarPage> {
  final TextEditingController _controller = TextEditingController();

  @override
  void initState() {
    super.initState();
    context.read<AzkarBloc>().add(FetchAzkar());
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Azkar Management'),
        centerTitle: true,
        actions: [
          IconButton(
            icon: const Icon(Icons.cloud_download_outlined),
            tooltip: 'Refresh',
            onPressed: () => context.read<AzkarBloc>().add(FetchAzkar()),
          ),
          const SizedBox(width: 8),
        ],
      ),
      drawer: const CustomDrawer(),
      body: BlocConsumer<AzkarBloc, AzkarState>(
        listener: (context, state) {
          if (state is AzkarLoaded) {
            _controller.text = state.content;
          } else if (state is AzkarError) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text(state.message),
                backgroundColor: Colors.redAccent,
              ),
            );
          } else if (state is AzkarSaved) {
            _showSuccessSnackBar();
          }
        },
        builder: (context, state) {
          return Column(
            children: [
              _buildEditorHeader(state is AzkarLoading),
              Expanded(
                child: Container(
                  margin: const EdgeInsets.fromLTRB(24, 0, 24, 24),
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(24),
                    border: Border.all(color: Colors.grey.shade100),
                    boxShadow: [
                      BoxShadow(
                        color: Colors.grey.shade50,
                        blurRadius: 20,
                        offset: const Offset(0, 10),
                      ),
                    ],
                  ),
                  child: state is AzkarLoading
                      ? const Center(child: CircularProgressIndicator())
                      : _buildJsonEditor(),
                ),
              ),
            ],
          );
        },
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () {
          context.read<AzkarBloc>().add(SaveAzkarEvent(_controller.text));
        },
        backgroundColor: AppColors.colorPrimary,
        foregroundColor: Colors.white,
        icon: const Icon(Icons.save_rounded),
        label: Text(
          'Save Changes',
          style: GoogleFonts.plusJakartaSans(fontWeight: FontWeight.bold),
        ),
      ),
    );
  }

  Widget _buildEditorHeader(bool isLoading) {
    return Padding(
      padding: const EdgeInsets.all(24.0),
      child: Row(
        children: [
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Azkar JSON Editor',
                style: GoogleFonts.plusJakartaSans(
                  fontSize: 24,
                  fontWeight: FontWeight.bold,
                  color: AppColors.colorPrimary,
                ),
              ),
              const SizedBox(height: 4),
              Text(
                'Edit the raw JSON data for azkar.json file.',
                style: GoogleFonts.plusJakartaSans(
                  fontSize: 14,
                  color: Colors.grey.shade500,
                ),
              ),
            ],
          ),
          const Spacer(),
          if (isLoading)
            const Text('Syncing...')
          else
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
              decoration: BoxDecoration(
                color: Colors.green.withValues(alpha: 0.1),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Row(
                children: [
                  const Icon(
                    Icons.check_circle_rounded,
                    color: Colors.green,
                    size: 14,
                  ),
                  const SizedBox(width: 6),
                  Text(
                    'Live from Storage',
                    style: GoogleFonts.plusJakartaSans(
                      fontSize: 12,
                      color: Colors.green,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildJsonEditor() {
    return TextField(
      controller: _controller,
      maxLines: null,
      style: GoogleFonts.firaCode(
        fontSize: 14,
        color: AppColors.text,
        height: 1.5,
      ),
      decoration: InputDecoration(
        hintText: 'Enter JSON content...',
        border: InputBorder.none,
        enabledBorder: InputBorder.none,
        focusedBorder: InputBorder.none,
        contentPadding: const EdgeInsets.all(24),
        fillColor: Colors.transparent,
      ),
    );
  }

  void _showSuccessSnackBar() {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Row(
          children: [
            const Icon(Icons.check_circle_rounded, color: Colors.white),
            const SizedBox(width: 12),
            const Text('Azkar saved and updated successfully!'),
          ],
        ),
        backgroundColor: Colors.green,
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        margin: const EdgeInsets.all(24),
      ),
    );
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }
}
