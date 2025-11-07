package com.example.udfreaderapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.palantir.isofilereader.GenericInternalIsoFile;
import java.io.File;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private List<GenericInternalIsoFile> fileList;

    public FileAdapter(List<GenericInternalIsoFile> fileList) {
        this.fileList = fileList;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_file, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        GenericInternalIsoFile file = fileList.get(position);
        holder.fileNameTextView.setText(file.getFullFileName(File.separatorChar));
    }

    @Override
    public int getItemCount() {
        return fileList != null ? fileList.size() : 0;
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView fileNameTextView;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameTextView = itemView.findViewById(R.id.textview_filename);
        }
    }
}
