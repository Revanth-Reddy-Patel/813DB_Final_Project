cols = length(X)/11;

X_matrix = reshape(X,cols, 11);
Y_matrix = reshape(Y,cols, 11);

for n = 1 : cols
    X_values = X_matrix(1,:);
    Y_values = Y_matrix(1,:);

    x_tran = transpose(X_values);
    y_tran = transpose(Y_values);

    pline(x_tran,y_tran,5)
end

disp(x_tran)
disp(y_tran)
